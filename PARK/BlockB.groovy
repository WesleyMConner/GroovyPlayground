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
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Matcher
import java.util.regex.Pattern

@Field static ConcurrentHashMap<String, ArrayList> MATCHERS = [:]

definition(
  name: 'BlockB',
  namespace: 'Wmc',
  author: 'Wesley M. Conner',
  description: 'Develop AsciiDoc-like parsing',
  singleInstance: true,
  iconUrl: '',
  iconX2Url: ''
)

preferences {
  page(name: 'BlockB')
}

Map BlockB() {
  return dynamicPage(
    name: 'BlockB',
    title: 'BlockB',
    install: true,
    uninstall: true
  ) {
    section {
      //paragraph 'Everything happens on installation. Click "Done".'
      processString()
    }
  }
}

// THESE SPECIFIC METHODS WILL BE REFACTORED AND CACHES WILL BE USED.

String replaceTrivial(String s) {
  return s.replaceAll(/\(C\)/, '©')
          .replaceAll(/\(R\)/, '®')
          .replaceAll(/\(TM\)/, '™')
          .replaceAll(/--/, '—')
          .replaceAll(/\.\.\./, '…')
          .replaceAll(/->/, '→')
          .replaceAll(/<-/, '←')
          .replaceAll(/(?s)(\ +\n)/, ' ')
}

Matcher getMatcher(String name) {
  Matcher matcher /* = MATCHERS[name] TEMPORARILY DO NOT USE THE CACHE */
  if (!matcher) {
    switch (name) {
      case 'Linebreak':
        matcher = Pattern.compile(/(?s)(\+\n)/).matcher('')
        break
      case 'Italic':
        matcher = Pattern.compile(/(?s)(_)(.*?)_/).matcher('')
        break
      case 'Bold':
        // Capture but skip asterisks that are part of a Bullet1..3.
        // See also extra adjustments required during replace.
        matcher = Pattern.compile(/(?m)(?!^\*{1,3} )(.*?)(\*)(?!^\*+)(.*?)\*/).matcher('')
        //                             ^^^^^^^^^^Ignore Bullet
        //                                          ^^^^^Group1
        //                                                   ^^^^^^^^Ignore Bullet
        //                                               ^^^^Group2
        //                                                           ^^^^^Group3
        //                                          ^^^^^^^^^^^^^^^^^^^^^^Iteration
        break
      case 'Mono':
        // Capture but skip '+' at the end of a line.
        matcher = Pattern.compile(/(?s)(\+)(.*?)\+/).matcher('')
        break
      case 'Superscript':
        matcher = Pattern.compile(/(?s)(\^)(.*?)\^/).matcher('')
        break
      case 'Subscript':
        matcher = Pattern.compile(/(?s)(~)(.*?)~/).matcher('')
        break
      case 'Command':
        matcher = Pattern.compile(/(?s)(`)(.*?)`/).matcher('')
        break
      case 'Foreground':
        // Consideratons
        //   - Do not accommodate nesting foreground/background colors.
        //   - Per https://stackoverflow.com/questions/1636350 it is tricky to
        //     accommodate "HTML colors", "CSS colors" and "HEX colors"; so,
        //     focus on ADOC syntax and do not nail down color syntax.
        matcher = Pattern.compile(/(?sm)(\[)(?![^\]]*-background)([^\]]*)(\]\#)(.*?)\#/).matcher('')
        //                              1   NEG-LOOKAHEAD        2       3     4
        break
      case 'Background':
        // See comments under 'Foreground'.
        //==>matcher = Pattern.compile(/(?sm)\[([^\]]*?)(-background\]\#)(.*?)\#/).matcher('')
        matcher = Pattern.compile(/(?sm)(\[)([^\]]*?)(-background\]\#)(.*?)\#/).matcher('')
        //                              1   2        3                4
        break
      case 'Big':
        matcher = Pattern.compile(/(?s)(\[big\])(\#)(.*?)\#/).matcher('')
        break
      case 'Huge':
        matcher = Pattern.compile(/(?s)(\[huge\])(\#)(.*?)\#/).matcher('')
        break
      case 'Heading1':
        matcher = Pattern.compile(/(?m)(^= )(.*?)$/).matcher('')
        break
      case 'Heading2':
        matcher = Pattern.compile(/(?m)(^== )(.*?)$/).matcher('')
        break
      case 'Heading3':
        matcher = Pattern.compile(/(?m)(^=== )(.*?)$/).matcher('')
        break
      case 'Tip':
        matcher = Pattern.compile(/(?m)(^TIP: )(.*?)$/).matcher('')
        break
      case 'Important':
        matcher = Pattern.compile(/(?m)(^IMPORTANT: )(.*?)$/).matcher('')
        break
      case 'Warning':
        matcher = Pattern.compile(/(?m)(^WARNING: )(.*?)$/).matcher('')
        break
      case 'Caution':
        matcher = Pattern.compile(/(?m)(^CAUTION: )(.*?)$/).matcher('')
        break
      case 'TermWithDefn':
        // Yields one Match Group if the Term is followed by BulletN.
        // Yields two Match Groups if the Term is followed by anything else.
        matcher = Pattern.compile(/(?sm)^([^\n]*)::(\n[^*].*?\n)?/).matcher('')
        break
      case 'Bullet1':
        //->matcher = Pattern.compile(/(?sm)(^\* )(.*?)\n(?:\n|\*|$)/).matcher('')
        matcher = Pattern.compile(/(?sm)(^\* )(.*?)\n(?:\n|\*|$)/).matcher('')
        break
      case 'Bullet2':
        matcher = Pattern.compile(/(?sm)(^\*\* )(.*?)\n(?:\n|\*|$)/).matcher('')
        break
      case 'Bullet3':
        matcher = Pattern.compile(/(?sm)(^\*\*\* )(.*?)\n(?:\n|\*|$)/).matcher('')
        break
      default:
        log.Error("getMatcher(): Unknown Matcher Name >${name}<")
    }
    // TEMPORARILY DO NOT USE THE CACHE
    // if (!encounteredError) { MATCHERS[name] = matcher }
  }
  return matcher
}

//(?sm)\[([^\]]*?)-background\](#)(.*?)\#


/*
  OUT-OF-SCOPE
    [%hardbreaks]
    :hardbreaks-option:
*/

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
  // MATCHERS['LineBreak'] = [//, ""]
  // MATCHERS['HBar'] = [//, ""]
  // MATCHERS['HorzTermWithDefn'] = [//, ""]
  // MATCHERS['QandA'] = [//, ""]
  // MATCHERS['Table'] = [//, ""]
  // MATCHERS['CSV'] = [//, ""]
*/

String informationalHtmlTable(String tag, String content, String color) {
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

String matchAndReplace(String sArg) {
  String s = sArg
  ArrayList targets = [
    //'Linebreak',
    'Italic',
    'Bold',
    'Mono',
    'Superscript',
    'Subscript',
    'Command',
    'Big',
    'Huge',
    'Background',
    'Foreground',
    'Heading1',
    'Heading2',
    'Heading3',
    'Tip',
    'Important',
    'Warning',
    'Caution',
    'TermWithDefn',
    'Bullet1',
    //'Bullet2',
    //'Bullet3',
  ]
        ArrayList xyz = s.split('\n')
        ArrayList preInfo = xyz.withIndex().collect { e, j ->
          "${j}..'${summarizeString(e)}'"
        }
          paragraph([
          '--------------------------------------------------',
          "AT TOP:",
          '--------------------------------------------------',
          *preInfo
        ].join('<br>'))

  targets.forEach{ target ->
//->paragraph("#236 target: ${target}")
    Matcher m = getMatcher(target)
//->paragraph("#238 matcher m: ${m}")
    // VERY IMPORTANT:
    //   - DO NOT USE "if (m) { ... }" WHICH RETURNS TRUE IF THE ASSOCIATED
    //     PATTERN MATCHES THE CURRENT STRING (initialized to null).
    //   - INSTEAD, USE "if (m != null) { ... }" WHICH CONFIRMS THE MATCHER
    //     EXISTS AND CAN ACCEPT A NEW STRING VIA reset(String s).
    if (m != null) {
      m.reset(s)
      ArrayList revisedS = []
      String carryover = ''
      Integer iterationStart = 0
      Integer maxLoops = 10
      Integer i = 0
      while ((i++ < maxLoops) && m.find()) {
        String before
        switch (target) {
          case 'Linebreak':
            // Consumes (\+$^) with nothing
            before = s.substring(iterationStart, m.start(1))
            replacer = "<br>"  // WARNING "" vs '' for HTML TAG.
            break
          case 'Italic':
            before = s.substring(iterationStart, m.start(1))
            replacer = "<em>${m.group(2)}</em>"
            break
          case 'Bold':
            String matched = m.group(3)
            if (matched) {
              // Bold the matched group #3.
              before = s.substring(iterationStart, m.start(2))
              replacer = "<b>${m.group(3)}</b>"
            } else {
              // There being no group #3, leave everything 'as was'.
              before = s.substring(iterationStart, m.end())
              replacer = ''
            }
            break
          case 'Mono':
            before = s.substring(iterationStart, m.start(1))
            replacer = "<tt>${m.group(2)}</tt>"
            break
          case 'Superscript':
            before = s.substring(iterationStart, m.start(1))
            replacer = "<sup>${m.group(2)}</sup>"
            break
          case 'Subscript':
            before = s.substring(iterationStart, m.start(1))
            replacer = "<sub>${m.group(2)}</sub>"
            break
          case 'Command':
            before = s.substring(iterationStart, m.start(1))
            replacer = "<code>${m.group(2)}</code>"
            break
          case 'Foreground':
            before = s.substring(iterationStart, m.start(1))
            replacer = "<span style='color: ${m.group(2)};'>${m.group(4)}</span>"
            break
          case 'Background':
            before = s.substring(iterationStart, m.start(1))
            replacer = "<span style='background: ${m.group(2)};'>${m.group(4)}</span>"
            break
          case 'Big':
            before = s.substring(iterationStart, m.start(1))
            replacer = "<span style='font-size: 1.1em;'>${m.group(3)}</span>"
            break
          case 'Huge':
            before = s.substring(iterationStart, m.start(1))
            replacer = "<span style='font-size: 1.3em;'>${m.group(3)}</span>"
            break
          case 'Heading1':
            before = s.substring(iterationStart, m.start(1))
            replacer = "<span style='font-size: 3.0em;'>${m.group(2)}</span>"
            break
          case 'Heading2':
            before = s.substring(iterationStart, m.start(1))
            replacer = "<span style='font-size: 2.0em;'>${m.group(2)}</span>"
            break
          case 'Heading3':
            before = s.substring(iterationStart, m.start(1))
            replacer = "<span style='font-size: 1.5em;'>${m.group(2)}</span>"
            break
          case 'Tip':
            before = s.substring(iterationStart, m.start(1))
            replacer = informationalHtmlTable('TIP', m.group(2), '#A0A0A0')
            break
          case 'Important':
            before = s.substring(iterationStart, m.start(1))
            replacer = informationalHtmlTable('IMPORTANT', m.group(2), '#1434A4')
            break
          case 'Warning':
            before = s.substring(iterationStart, m.start(1))
            replacer = informationalHtmlTable('WARNING', m.group(2), '#D22B2B')
            break
          case 'Caution':
            before = s.substring(iterationStart, m.start(1))
            replacer = informationalHtmlTable('CAUTION', m.group(2), '#FFEA00')
            break
          case 'TermWithDefn':
            String defn = m.group(2)
            if (defn) {
              // Provide Term and Defn
              before = s.substring(iterationStart, m.start(1))
              replacer = "<b>${m.group(1)}</b>::<br><ul>${m.group(2)}</ul>"
            } else {
              // Provide Term and defer Defn to Bullet# formatting.
              before = s.substring(iterationStart, m.start(1))
              replacer = "<b>${m.group(1)}</b>::<br>"
            }
            break
          case 'Bullet1':
    paragraph('Entered Bullet1')
            before = s.substring(iterationStart, m.start(1))
            replacer = "<ul><li>${m.group(2)}</li></ul>"
            break
          case 'Bullet2':
    paragraph('Entered Bullet2')
            before = s.substring(iterationStart, m.start(1))
            replacer = "<ul><ul><li>${m.group(2)}</li></ul></ul>"
            break
          case 'Bullet3':
    paragraph('Entered Bullet3')
            before = s.substring(iterationStart, m.start(1))
            replacer = "<ul><ul><ul><li>${m.group(2)}</li></ul></ul>"
            break
          default:
            encounteredError = true
        }
        carryover = s.substring(m.end(), m.regionEnd())
        iterationStart = m.end()
        revisedS << before
        revisedS << replacer
        ArrayList info = revisedS.withIndex().collect { e, j ->
          "${j}..'${summarizeString(e)}'"
        }
        paragraph([
          '--------------------------------------------------',
          "AFTER ${target} Loop #${i}:",
          '--------------------------------------------------',
          *info
        ].join('<br>'))
      }
      revisedS << carryover
      s = revisedS.join()
    } else {
      paragraph("No matcher for ${target}")
    }
  }
  return s
}

void processString() {
  //populatePatternAndMatcherCaches()
  String s = getSampleData()
  s = replaceTrivial(s)
  //-> paragraph("----<br>AFTER replaceTrivial(s)<br>${s}<br>----<br>")
  s = matchAndReplace(s)
  paragraph([
    'F I N A L   R E S U L T S',
    '------------------------------------------------',
    s,
    '------------------------------------------------'
  ].join('<br>'))
}

ArrayList replaceBlock(ArrayList blocklist, Integer pos, ArrayList replacement) {
  blocklist[pos] = replacement
  return blocklist.flatten()
}

ArrayList applyPatternNameToBlock(String patternName, ArrayList block) {
  ArrayList newBlocks = []
  String blkType = block[0]
  String blkData = block[1]
  ArrayList orderedPatterns = blocksToOrderedPatterns[blkType]
  Pattern pattern = patterns[patternName]
  Matcher m = (blkData =~ pattern)
  // Instead of looping, do a "one and done"
  if (m.find()) {
    if (m.start() > 0) {
      nextBlocks << ['raw', blkData.substring(0, m.start())]
    }
    nextBlocks << [patternName, "<b>${m.group(1)}</b>"]
    if (m.end() != m.regionEnd()) {
      nextBlocks << ['raw', blkData.substring(m.end(), m.regionEnd())]
    }
  }
  return newBlocks
}

String redEllipse() {
  return '''<span style='color: red;'><b>...</b></span>'''
}

String summarizeString(String sArg) {
  // Ensure the summarized string is <= 100 chars.
  s = sArg
  s.replaceAll(/\n/, '␤')
  s.replaceAll('<br>', '⮐')
  Integer l = s.size()
  return (l > 60)
    ? "${s.substring(0, 29)}${redEllipse()}${s.substring(l - 29, l - 1)}"
    : s
}

void showBlocks(ArrayList blocks) {
  if (blocks) {
    blocks.eachWithIndex { block, i ->
      String type = block[0]
      String data = block[1]
      data = data.replace('\n', '␤')
      paragraph("[blk #${i} ${type}] ${summarizeString(data)}")
    }
  } else {
    paragraph('showBlocks() has null argument')
  }
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
  log.info('uninstalled setting MATCHERS to an empty map.')
  MATCHERS = [:]
}

void f() {
  log.info('f() For development: Emptying MATCHERS cache on installed and updated.')
  MATCHERS = [:]
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

normal paragraph 3

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

 This text should be presented 'as is' with no formatting. This text should be
 presented 'as is' with no formatting. This text should be presented 'as is'
 with no formatting. This text should be presented 'as is' with no formatting.
 This text should be presented 'as is' with no formatting. This text should be
 presented 'as is' with no formatting. This text should be presented 'as is'
 with no formatting.

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

Let's test a few inline references. First a true string: >s1<. Next a list: >list1<.
Nest a sample map: >map1<. That's it for now.

TIP: This is a tip.

IMPORTANT: This is important.

WARNING: This is a warning.

CAUTION: This is a caution.
'''
}
