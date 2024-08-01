// ---------------------------------------------------------------------------------
// T E S T B E D 3
//
// Copyright (C) 2023-Present Wesley M. Conner
//
// LICENSE
// Licensed under the Apache License, Version 2.0 (aka Apache-2.0, the
// "License"), see http://www.apache.org/licenses/LICENSE-2.0. You may
// not use this file except in compliance with the License. Unless
// required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
// implied.
// ---------------------------------------------------------------------------------
// For reference:
//   Unicode 2190 ← LEFTWARDS ARROW
//   Unicode 2192 → RIGHTWARDS ARROW

import com.hubitat.app.ChildDeviceWrapper as ChildDevW
import groovy.transform.Field
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Matcher
import java.util.regex.Pattern
import groovy.lang.Closure

@Field static ConcurrentHashMap<String, Map> TERMS = [:]
@Field static ConcurrentHashMap<String, ArrayList> BLOCK_STASH = [:]
@Field static Closure stashContent = { Matcher m ->
  String result = '>>>stashContent() ERROR<<<'
  if (m.group(2)) {
    String stashKey = java.time.Instant.now()
    BLOCK_STASH[stashKey] = m.group(2)
    result = "{{${stashKey}}}"
  }
  return result
}
@Field static Closure retrieveContent = { Matcher m ->
  return BLOCK_STASH[m.group(2)]
}
@Field static Closure infoHtmlTable = { String tag, String content, String color ->
  return """
    <table width='80%'>
      <tr style='vertical-align: center; text-align: center;'>
        <td width='20%' style='height: 80px; border-right: 3mm ${color} solid;
          font-size: 1.1em; text-align: center;'>
          <b>${tag}</b>
        </td>
        <td style='text-align: left'>
          ${content}
        </td>
      </tr>
    </table>"""
}
@Field static Closure displayTip = { Matcher m, String sIn, Closure replacerFn ->
  String result = '>>>displayTip() ERROR<<<'
  if (m.group(2)) {
    result = infoHtmlTable('TIP', m.group(2), '#A0A0A0')
  }
  return result
}
@Field static Closure displayImportant = { Matcher m, String sIn, Closure replacerFn ->
  String result = '>>>displayImportant() ERROR<<<'
  if (m.group(2)) {
    result = infoHtmlTable('IMPORTANT', m.group(2), '#1434A4')
  }
  return result
}
@Field static Closure displayWarning = { Matcher m, String sIn, Closure replacerFn ->
  String result = '>>>displayWarning() ERROR<<<'
  if (m.group(2)) {
    result = infoHtmlTable('WARNING', m.group(2), '#D22B2B')
  }
  return result
}
@Field static Closure displayCaution = { Matcher m, String sIn, Closure replacerFn ->
  String result = '>>>displayCaution() ERROR<<<'
  if (m.group(2)) {
    result = infoHtmlTable('CAUTION', m.group(2), '#FFEA00')
  }
  return result
}
@Field static Closure replaceAllWithFn = { Matcher m,
                                           String sIn,
                                           Closure replacerFn ->
  m.reset(sIn)
  ArrayList outBlks = []
  Integer lStart = 0
  String carryover
  while (m.find()) {
    outBlks << sIn.substring(lStart, m.start(1))
    outBlks << replacerFn(m)
    lStart = m.end()
    carryover = sIn.substring(m.end(), sIn.size())
  }
  outBlks << carryover
  return outBlks.join()
}

definition(
  name: 'BlockF',
  namespace: 'Wmc',
  author: 'Wesley M. Conner',
  description: 'Develop AsciiDoc-like parsing',
  singleInstance: true,
  iconUrl: '',
  iconX2Url: ''
)

preferences {
  page(name: 'BlockF')
}

Map BlockF() {
  return dynamicPage(
    name: 'BlockF',
    title: 'BlockF',
    install: true,
    uninstall: true
  ) {
    section {
      processString()
    }
  }
}

String replaceAllStrings(String s) {
  return s.replaceAll(/\(C\)/, '©')
          .replaceAll(/\(R\)/, '®')
          .replaceAll(/\(TM\)/, '™')
          .replaceAll(/--/, '—')
          .replaceAll(/\.\.\./, '…')
          .replaceAll(/->/, '→')
          .replaceAll(/<-/, '←')
          .replaceAll(/(?s)(\+\n)/, '<br>')
}

void addTerm(Map parms) {
  // Abstract
  //   Creates a RegExp Matcher wish robust replaceAll() capabilities.
  // Parms:
  //         name: String
  //           re: String
  //   replacerFn: Closure .. implies use of replaceAllWithFn()
  //     replacer: String  .. implies native Matcher.replaceAll()
  Map t = parms << [m: Pattern.compile(parms.re).matcher('')]
  // Add an appropriate replaceAll() capability.
  t['replaceAll'] = { String sIn ->
    String sOut
    if (t.replacerFn) {
      sOut = replaceAllWithFn(t.m, sIn, t.replacerFn)
    } else {
      sOut = t.m.replaceAll(t.replacer)
    }
    return sOut
  }
  TERMS[parms.name] = t
}

/*
  // MATCHERS['ParaTitle'] = [//, ""]
  // MATCHERS['Para'] = [//, ""]
  // MATCHERS['Literal'] = [//, ""]
  // MATCHERS['Note'] = [//, ""]
  // MATCHERS['ListingBlock'] = [//, ""]
  // MATCHERS['SidebarBlock'] = [//, ""]
  // MATCHERS['ExampleBlock'] = [//, ""]
  // MATCHERS['LiteralBlock'] = [//, ""]
  // MATCHERS['QuoteBlock'] = [//, ""]
  // MATCHERS['AsIs'] = [//, ""]
  // MATCHERS['HBar'] = [//, ""]
  // MATCHERS['HorzTermWithDefn'] = [//, ""]
  // MATCHERS['QandA'] = [//, ""]
  // MATCHERS['Table'] = [//, ""]
  // MATCHERS['CSV'] = [//, ""]
*/

void initializeTerms() {
  // NOTES:
  //   If this code comes to rest in an App | Device inclusion of terms
  //   could be selectable with a series of input(...) selections.
  addTerm(
    name: 'PassthroughRange',
    re: /(?ms)(^\+\+\+\+$)(.*?)(^\+\+\+\+$)/,
    replacerFn: stashContent
  )
  addTerm(
    name: 'PassthroughPara',
    re: /(?ms)(^\[pass\]\n)(.*?\n)(\n|$)/,
    replacerFn: stashContent
  )
  addTerm(
    name: 'PassthroughBlock',
    re: /(?ms)(pass:\[)([^\]]*?)(\])/,
    replacerFn: stashContent
  )
  addTerm(
    name: 'RestoreRef',
    re: /(\{\{)(.*?)(\}\})/,
    replacerFn: retrieveContent
  )
  addTerm(
    name: 'Linebreak',
    re: /(?s)(\+\n)/,
    replacer: '<br>'  // Consumes (\+$^) with no replacement
  )
  addTerm(
    name: 'Italic',
    re: /(?s)(_)(.*?)_/,
    replacer: '''<em>$2</em>'''
  )
  addTerm(
    name: 'Bold',
    re: /(?m)(?!^\*{1,3} )[^\*]*?(\*)(?!^\*+)(.*?)\*/,
    replacer: '''<b>$2</b>'''
  )
  addTerm(
    name: 'Mono',
    re: /(?s)(\+)(.*?)\+/,
    replacer: '''<tt>$2</tt>'''
  )
  addTerm(
    name: 'Superscript',
    re: /(?s)(\^)(.*?)\^/,
    replacer: '''<sup>$2</sup>'''
  )
  addTerm(
    name: 'Subscript',
    re: /(?s)(~)(.*?)~/,
    replacer: '''<sub>$2</sub>'''
  )
  addTerm(
    name: 'Command',
    re: /(?s)(`)(.*?)`/,
    replacer: '''<code>$2</code>'''
  )
  addTerm(
    name: 'Foreground',
    re: /(?sm)(\[)(?![^\]]*-background)([^\]]*)(\]\#)(.*?)\#/,
    replacer: '''<span style='color: $2;'>$4</span>'''
  )
  addTerm(
    name: 'Background',
    re: /(?sm)(\[)([^\]]*?)(-background\]\#)(.*?)\#/,
    replacer: '''<span style='background: $2;'>$4</span>'''
  )
  addTerm(
    name: 'Big',
    re: /(?s)(\[big\])(\#)(.*?)\#/,
    replacer: '''<span style='font-size: 1.1em;'>$3</span>'''
  )
  addTerm(
    name: 'Huge',
    re: /(?s)(\[huge\])(\#)(.*?)\#/,
    replacer: '''<span style='font-size: 1.3em;'>$3</span>'''
  )
  addTerm(
    name: 'Heading1',
    re: /(?m)(^= )(.*?)$/,
    replacer: '''<span style='font-size: 3.0em;'>$2</span>'''
  )
  addTerm(
    name: 'Heading2',
    re: /(?m)(^== )(.*?)$/,
    replacer: '''<span style='font-size: 2.0em;'>$2</span>'''
  )
  addTerm(
    name: 'Heading3',
    re: /(?m)(^=== )(.*?)$/,
    replacer: '''<span style='font-size: 1.5em;'>$2</span>'''
  )
  addTerm(
    name: 'Tip',
    re: /(?m)(^TIP: )(.*?)$/,
    replacerFn: displayTip
  )
  addTerm(
    name: 'Important',
    re: /(?m)(^IMPORTANT: )(.*?)$/,
    replacerFn: displayImportant
  )
  addTerm(
    name: 'Warning',
    re: /(?m)(^WARNING: )(.*?)$/,
    replacerFn: displayWarning
  )
  addTerm(
    name: 'Caution',
    re: /(?m)(^CAUTION: )(.*?)$/,
    replacerFn: displayCaution
  )
  addTerm(
    name: 'TermWithDefn',
    re: /(?sm)^([^\n]*)::\n(?!\*)(.+?)\n\n/,
    replacer: '''<b>$1</b>::<br><ul>$2</ul>'''
  )
  addTerm(
    name: 'TermWithBullets',
    re: /(?sm)^([^\n]*)(::)(?:\n\*)/,
    replacer: '''<b>$1</b>::<br>'''
  )
  addTerm(
    name: 'Bullet1',
    re: /(?sm)(^\* )(.*?)\n(?:\n|\*|$)/,
    replacer: '''<ul><li>$2</li></ul>'''
  )
  addTerm(
    name: 'Bullet2',
    re: /(?sm)(^\*\* )(.*?)\n(?:\n|\*|$)/,
    replacer: '''<ul><ul><li>$2</li></ul></ul>'''
  )
  addTerm(
    name: 'Bullet3',
    re: /(?sm)(^\*\*\* )(.*?)\n(?:\n|\*|$)/,
    replacer: '''<ul><ul><ul><li>$2</li></ul></ul>'''
  )
}

void processString() {
  initializeTerms()
  // Test TERMS extraction
  String s = getSampleData()
  //-> paragraph("<b>ITERATION getSampleData():</b><br>>${s}<")
  s = replaceAllStrings(s)
  paragraph("<b>ITERATION replaceAllStrings():</b>:<br>>${s}<")
  s = applyTerms(s)
  //-> paragraph("<b>ITERATION FINAL:</b><br>>${s}<")
}

// THESE SPECIFIC METHODS WILL BE REFACTORED AND CACHES WILL BE USED.

String applyTerms(String sArg) {
  String s = sArg
  [
    'PassthroughRange',
    /*
    'PassthroughPara',
    'PassthroughBlock',
    'Linebreak',
    'Italic',
    'Bold',
    'Mono',
    'Superscript',
    'Subscript',
    'Command',
    'Foreground',
    'Background',
    'Big',
    'Huge',
    'Heading1',
    'Heading2',
    'Heading3',
    'Tip',
    'Important',
    'Warning',
    'Caution',
    'TermWithDefn',
    'TermWithBullets',
    'Bullet1',
    'Bullet2',
    'Bullet3',
    'RestoreRef',
    */
  ].each { term ->
    Map t = TERMS[term]
    s = t.replaceAll(s)
    paragraph("<b>ITERATION for ${term}:</b>:<br>>${s}<")
  }
  return s
}

String redEllipse() {
  return '''<span style='color: red;'><b>…</b></span>'''
}

String glimpseString(String sArg) {
  String s = sArg.replaceAll(/\n/, '␤')
                 .replaceAll(/\r/, '␍')
                 .replaceAll(/\s/, '▪')
  Integer l = s.size()
  return (l > 60)
    ? "⦗${s.substring(0, 29)}⦘${redEllipse()}⦗${s.substring(l - 29, l - 1)}⦘"
    : s
}

// CORE METHODS

void installed() {
  // Called when a bare device is first constructed.
  f()
}

void updated() {
  // Called when a human uses the Hubitat GUI's Device drilldown page to edit
  // preferences (aka settings) AND presses 'Save Preferences'.
  f()
}

void uninstalled() {
  // Called on device tear down.
  f()
}

void f() {
  log.info('f() For development: Emptying TERMS & BLOCK_STASH.')
  TERMS = [:]
  BLOCK_STASH = [:]
}

// Sample Data
String getSampleData() {
  String s1 = 'This is a test of the emergency broadcast system.'
  List list1 = ['one', 'two', 'three', 'four', 'five']
  Map map1 = [a: 'apple', b: 'banana', g: 'grape', l:'lemon', o: 'orange']
  String testData = '''
1234567 101234567 201234567 301234567 401234567 501234567 601234567 701234567

1234567 101234567 201234567 30

1234567 101234567 20

= First *Header* is #1
normal paragraph -- 1

== Second _Header_ is #2
normal paragraph 2

* My father had a small estate in Nottinghamshire; I was the third of five sons.
He sent me to Emanuel College in Cambridge at fourteen years old, where I resided
three years, and applied myself close to my studies; but the charge of maintaining
me, although I had a very scanty allowance, being too great for a narrow fortune,
I was bound apprentice to Mr. James Bates, an eminent surgeon in London, with
whom I continued four years. My father now and then sending me small sums of
money, I laid them out in learning navigation, and other parts of the mathematics,
useful to those who intend to travel, as I always believed it would be, some
time or other, my fortune to do. When I left Mr. Bates, I went down to my
father: where, by the assistance of him and my uncle John, and some other
relations, I got forty pounds, and a promise of thirty pounds a year to maintain
me at Leyden: there I studied physic two years and seven months, knowing it
would be useful in long voyages. _GULLIVER’S TRAVELS

** Soon after my return from Leyden, I was recommended by my good master, Mr.
Bates, to be surgeon to the Swallow, Captain Abraham Pannel, commander; with
whom I continued three years and a half, making a voyage or two into the Levant,
and some other parts. When I came back I resolved to settle in London; to which
Mr. Bates, my master, encouraged me, and by him I was recommended to several
patients. I took part of a small house in the Old Jewry; and being advised to
alter my condition, I married Mrs. Mary Burton, second daughter to Mr. Edmund
Burton, hosier, in Newgate-street, with whom I received four hundred pounds
for a portion. _GULLIVER’S TRAVELS

*** But my good master Bates dying in two years after, and I having few friends,
my business began to fail; for my conscience would not suffer me to imitate the
bad practice of too many among my brethren. Having therefore consulted with my
wife, and some of my acquaintance, I determined to go again to sea. I was
surgeon successively in two ships, and made several voyages, for six years, to
the East and West Indies, by which I got some addition to my fortune. My hours
of leisure I spent in reading the best authors, ancient and modern, being always
provided with a good number of books; and when I was ashore, in observing the
manners and dispositions of the people, as well as learning their language;
wherein I had a great facility, by the strength of my memory. _GULLIVER’S
TRAVELS

=== This is `Header 3`

normal paragraph 3 +
with continuation to next line.

normal paragraph 4 that keeps on `going and going and going` and going and going
and going and going and going and going and ... going and going and going and
going and going.

descriptive list1::
descriptive list information ... one two three four five
descriptive list2::
* My father had a small estate in Nottinghamshire; I was the third of five sons.
He sent me to Emanuel College in Cambridge at fourteen years old, where I resided
three years, and applied myself close to my studies; but the charge of maintaining
me, although I had a very scanty allowance, being too great for a narrow fortune,
I was bound apprentice to Mr. James Bates, an eminent surgeon in London, with
whom I continued four years. My father now and then sending me small sums of
money, I laid them out in learning navigation, and other parts of the mathematics,
useful to those who intend to travel, as I always believed it would be, some
time or other, my fortune to do. When I left Mr. Bates, I went down to my
father: where, by the assistance of him and my uncle John, and some other
relations, I got forty pounds, and a promise of thirty pounds a year to maintain
me at Leyden: there I studied physic two years and seven months, knowing it
would be useful in long voyages. _GULLIVER’S TRAVELS

** Soon after my return from Leyden, I was recommended by my good master, Mr.
Bates, to be surgeon to the Swallow, Captain Abraham Pannel, commander; with
whom I continued three years and a half, making a voyage or two into the Levant,
and some other parts. When I came back I resolved to settle in London; to which
Mr. Bates, my master, encouraged me, and by him I was recommended to several
patients. I took part of a small~house~ in the Old Jewry; and being advised to
alter my condition, I married Mrs. Mary Burton, second daughter to Mr. Edmund
Burton, hosier, in Newgate-street, with whom I received four hundred pounds
for a portion. _GULLIVER’S TRAVELS

*** But my good master Bates dying in two years after, and I having few friends,
my business began to fail; for my conscience would not suffer me to imitate the
bad practice of too many among my brethren. Having therefore consulted with my
wife, and some of my acquaintance, I determined to go again to sea. I was
surgeon successively in two ships, and made several voyages, for six years, to
the East and West Indies, by which I got some addition to my fortune. My hours
of leisure I spent in reading the best authors, ancient and modern, being always
provided with a good number of books; and when I was ashore, in observing the
manners and dispositions of the people, as well as learning their language;
wherein I had a great facility, by the strength of my memory. _GULLIVER’S
TRAVELS

.Code Block (leading spaces)

 This text should be presented 'as is' with no formatting. This text should be
 presented 'as is' with no formatting. This text should be presented 'as is'
 with no formatting. This text should be presented 'as is' with no formatting.
 This text should be presented 'as is' with no formatting. This text should be
 presented 'as is' with no formatting. This text should be presented 'as is'
 with no formatting.

.Passthrough Block
START OF BLOCK

++++
=Ignored Level1 Header

This text should be presented *as is* with no _formatting_. [blue]#this blue text#. This text should be
presented `as is` with no formatting. This text should be presented 'as is'
with no formatting. This text should be presented 'as is' with no formatting.
This text should be presented 'as is' with no formatting. This text should be
presented 'as is' with no formatting. This text should be presented 'as is'
 with no formatting.
++++

END OF BLOCK

.Normal Paragraph
We the *People of the United States*, in Order to form a *more perfect Union*,
establish Justice, insure domestic Tranquility, provide for the common defense,
promote the general Welfare, and secure the Blessings of Liberty to ourselves
and our Posterity, do ordain and establish this Constitution for the United
States of America.

.Normal Header with Pass Paragraph
[pass]
We the *People of the United States*, in Order to form a *more perfect Union*,
establish Justice, insure domestic Tranquility, provide for the common defense,
promote the general Welfare, and secure the Blessings of Liberty to ourselves
and our Posterity, do ordain and establish this Constitution for the United
States of America.

[pass]
.Header and Paragraph with Pass Construct
We the *People of the United States*, in Order to form a *more perfect Union*,
establish Justice, insure domestic Tranquility, provide for the common defense,
promote the general Welfare, and secure the Blessings of Liberty to ourselves
and our Posterity, do ordain and establish this Constitution for the United
States of America.

.Inline Passthrough Examples
FIRST EXAMPLE: pass:[content like #{variable} passed directly to the output] followed by normal content.

SECOND EXAMPLE: content with only select substitutions applied: pass:[__<{John.Smith@google.com}>__]

.Character-Level Escaping examples
This is \\*not bold*, \\_not emphasized_, X\\^not_superscript^,
\\https://google.com/[Google.com]

= Yet Another Level1 Heading

.Mixed Font Features
Here is some text with various things embedded, including: this^superscript^,
this~subscript~, `thisCommand`, [blue]#this blue text#, [red]#this red text#
[yellow-background]#this text with yellow background#.
[#90EE90-background]#This is text with lightgreen (90EE90) background#. Here is
[big]#some big text#. Here is [huge]#some huge text#. Back to normal text.

Line that does not have mono text.

Line that +does have mono+ text.

Line that `does have command` text.

.This acts as a simple header
This is a paragraph that runs for more than one line. It's just here* to occupy
space and flesh-out testing. For the most part, it can be ignored.

.Special Text
Copyright(C),TemporaryMark(TM),Registered(R),EmDash--,Ellipse...,
RightArrow->,LeftArrow<-

[cols = 3]
|===
|row 1 col 1
|row 1 col 2 +
more row 1 col 2
|row 1 col 3
|row 2 col 1 |row 2 col 2
|row 2 col 3
|row 3 col 1 +
More for row3 col1
|row 3 col 2 |row 3 col 3
|===

== Check special character replacements
  Example 1: (C)
  Example 2: (R)
  Example 3: (TM)
  Example 4: --
  Example 5: ...
  Example 6: ->
  Example 7: <-

Let's test a few inline references. First a true string: *${$s1}*. Next a
list: [bList]#${list1}#. Nest a sample map: [bMap]#${map1}#. That's it for now.

TIP: This is a tip.

IMPORTANT: This is important.

WARNING: This is a warning.

CAUTION: This is a caution.
'''
}
