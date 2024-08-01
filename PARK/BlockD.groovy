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

@Field static ConcurrentHashMap<String, ArrayList> MATCHERS = [:]
@Field static ConcurrentHashMap<String, ArrayList> BLOCK_STASH = [:]

definition(
  name: 'BlockD',
  namespace: 'Wmc',
  author: 'Wesley M. Conner',
  description: 'Develop AsciiDoc-like parsing',
  singleInstance: true,
  iconUrl: '',
  iconX2Url: ''
)

preferences {
  page(name: 'BlockD')
}

Map BlockD() {
  return dynamicPage(
    name: 'BlockD',
    title: 'BlockD',
    install: true,
    uninstall: true
  ) {
    section {
      processString()
    }
  }
}

// THESE SPECIFIC METHODS WILL BE REFACTORED AND CACHES WILL BE USED.

Matcher getMatcher(String name) {
  Matcher matcher = MATCHERS[name]
  if (!matcher) {
    switch (name) {
      case 'PassthroughRange':
        String rangeRE = /(^\+\+\+\+$)(.*?)(^\+\+\+\+$)/
        String paraRE = /(^(\[pass]\n)(.*?)\n(\n|$))/
        matcher = Pattern.compile(/(?ms)$rangeRE|$paraRE/).matcher('')
        break
      case 'RestoreRef':
        matcher = Pattern.compile(/(\{\{)(.*?)(\}\})/).matcher('')
        break
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
        log.Error("getMatcher(): Unknown Matcher Name ${name}")
    }
    if (!encounteredError) { MATCHERS[name] = matcher }
  }
  return matcher
}

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

Boolean blksFinal(ArrayList blks) {
  Integer totalBlocks = blks.size()
  Integer finalBlocks = 0
  blks.each{ blk -> if (blk.t == 'Final') { ++finalBlocks } }
  paragraph("blksFinal() ${finalBlocks} of ${totalBlocks}")
  return finalBlocks == totalBlocks
}

String matchAndReplace(String sArg) {
  ArrayList allParsers = [
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
    'Bullet2',
    'Bullet3',
  ]
  Integer abortCountdown = 50
  ArrayList blks = [ [ t: 'Raw', s: sArg ] ]
  Map blkTargets = [
    Raw: [ 'Heading1' ]
  ]
  Integer i = 0
  while (abortCountdown != 0 && !blksFinal(blks)) {
    if (abortCountdown == 0) {
      paragraph("${i}: ***** FORCING ABORT *****")
      return 'ERROR'
    }
    abortCountdown--
    blks.eachWithIndex { blk, j ->
      paragraph("${i}.${j}: Starting ${glimpseBlock(blk)}")
      ArrayList parsers = blkTargets[blk.t]
      if (parsers) {
        parsers.eachWithIndex { parserName, k ->
          String index = "${i}.${j}.${k}"
          paragraph("${index}: Applying ${parserName} parser")
          ArrayList newblks = applyTargetToBlock(index, blk, parserName)
          paragraph("${index}: BRUTE FORCE EXIT!")
          abortCountdown = 0
        }
      } else {
        // If there are no parsers for the block, mark its type as Final
        ////        blk.t = 'Final'
      }
    }
  }
  String output = ( blks.collect { blk -> blk.s } ).join()
  paragraph([
    '-------------------------------------',
    '       matchAndReplace() FINAL',
    '-------------------------------------',
    showBlocks(blks),
    '-------------------------------------'
  ].join('<br/>'))
  return output
}

ArrayList applyTargetToBlock(String index, Map blk, String parserName) {
  ArrayList newBlks = []
  Matcher m = getMatcher(parserName)
  // VERY IMPORTANT:
  //   if (m != null) { ... } - TRUE if a Matcher exists.
  //          if (m) { ... }" - TRUE if current string/index matches pattern.
  if (m != null) {  // NOTE: if (m) { ... } returns
  paragraph('Parser reset')
    m.reset(blk.s)
    // Inside find() loops, 'postMatch' is managed by the parser?!
    // On exiting find(), a final raw 'postMatch' block is appended to newBlks.
    String postMatch = ''
    Integer l = 0
    Integer lStart = 0
    String idx
    // TEMPORARILY PUT A LOW MAX ON LOOPS TO AVOID RUNAWAY (BAD) MATCHING
    while ((l < 10) && m.find()) {
      idx = "${index}.${l}"
      l++
      paragraph([
        "l: ${l}",
        "idx: ${idx}",
        "m.regionStart(): ${m.regionStart()}",
        "m.regionEnd(): ${m.regionEnd()}",
        "m.start(): ${m.start()}",
        "m.end(): ${m.end()}",
        "m.start(1): ${m.start(1)}",
        "m.end(1): ${m.end(1)}",
        "m.start(2): ${m.start(2)}",
        "m.end(2): ${m.end(2)}"
      ].join('<br>'))
      switch (parserName) {
        case 'Linebreak':
          // Consumes (\+$^) with nothing
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Final', s: '<br>' ]
          break
        case 'Italic':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Final', s: "<em>${m.group(2)}</em>" ]
          break
        case 'Bold':
          String matched = m.group(3)
          if (matched) {
            // Bold the matched group #3.
            newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(2)) ]
            newBlks << [ t: 'Final', s: "<b>${m.group(3)}</b>" ]
          } else {
            // There being no group #3, leave everything 'as was'.
            newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.end) ]
            // NB: There is no match replacement.
          }
          break
        case 'Mono':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Final', s: "<tt>${m.group(2)}</tt>" ]
          break
        case 'Superscript':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Final', s: "<sup>${m.group(2)}</sup>" ]
          break
        case 'Subscript':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Final', s: "<sub>${m.group(2)}</sub>" ]
          break
        case 'Command':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Final', s: "<code>${m.group(2)}</code>" ]
          break
        case 'Foreground':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: blk.t,
            s: "<span style='color: ${m.group(2)};'>${m.group(4)}</span>" ]
          break
        case 'Background':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: blk.t,
            s: "<span style='background: ${m.group(2)};'>${m.group(4)}</span>" ]
          break
        case 'Big':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: blk.t,
            s: "<span style='font-size: 1.1em;'>${m.group(3)}</span>" ]
          break
        case 'Huge':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: blk.t,
            s: "<span style='font-size: 1.3em;'>${m.group(3)}</span>" ]
          break
        case 'Heading1':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Heading',
            s: "<span style='font-size: 3.0em;'>${m.group(2)}</span>" ]
          showBlocks(newBlks)
          //String newS = m.replaceAll('''<span style='font-size: 3.0em;'>$2</span>''')
          paragraph("Heading w/ blocks ${showBlocks(newBlks)}")
          break
        case 'Heading2':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Heading',
            s: "<span style='font-size: 2.0em;'>${m.group(2)}</span>" ]
          break
        case 'Heading3':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Heading',
            s: "<span style='font-size: 1.5em;'>${m.group(2)}</span>" ]
          break
        case 'Tip':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Info',
            s: informationalHtmlTable('TIP', m.group(2), '#A0A0A0') ]
          break
        case 'Important':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Info',
            s: informationalHtmlTable('IMPORTANT', m.group(2), '#1434A4') ]
          break
        case 'Warning':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Info',
            s: informationalHtmlTable('WARNING', m.group(2), '#D22B2B') ]
          break
        case 'Caution':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Info',
            s: informationalHtmlTable('CAUTION', m.group(2), '#FFEA00') ]
          break
        case 'TermWithDefn':
          String defn = m.group(2)
          if (defn) {
            // Provide Term and Defn
            newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
            newBlks << [ t: 'Heading', s: "<b>${m.group(1)}</b>::<br>"]
            newBlks << [ t: 'blk.t', s: "<ul>${m.group(2)}</ul>" ]
          } else {
            // Provide Term and defer Defn to Bullet# formatting.
            newBlks << [ t: 'Heading', s: blk.s.substring(lStart, m.start(1)) ]
            newBlks << [ t: 'Heading', s: "<b>${m.group(1)}</b>::<br>"]
          }
          break
        case 'Bullet1':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Bullet',
            s: "<ul><li>${m.group(2)}</li></ul>" ]
          break
        case 'Bullet2':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Bullet',
            s: "<ul><ul><li>${m.group(2)}</li></ul></ul>"]
          break
        case 'Bullet3':
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Bullet',
            s: "<ul><ul><ul><li>${m.group(2)}</li></ul></ul>"]
          break
        default:
          encounteredError = true
      }
      postMatch = blk.s.substring(m.end(), blk.s.size())
      lStart = m.end()
    }
    idx = "${index}.${l}"
    if (newBlks.size() > 0) {
      if (postMatch) { newBlks << [ t: blk.t, s: postMatch ] }
      paragraph("${idx}: ${parserName} produced<br>${showBlocks(newBlks)}")
    } else {
      newBlks = [ [t: 'Final', s: blk.s ] ]
      paragraph("${idx}: ${parserName} 'Final'<br>${newBlks}")
    }
  } else {
    paragraph("ERROR: Unale to retrieve Matcher for ${parserName}")
  }
  return newBlks
}

void processString() {
  String sIn = getSampleData()
  Matcher m = getMatcher('Heading1')
  m.reset(sIn)
  String t = now()
  //String sOut = m.replaceAll("<span style='font-size: 3.0em;'>$2</span>")
  //paragraph("sOut = ${sOut}")
  String sOut = m.replaceAll("<span style='font-size: 3.0em;'>$2</span>")
  paragraph("sOut = ${sOut}")
}

String redEllipse() {
  return '''<span style='color: red;'><b>…</b></span>'''
}

String glimpseBlock(Map blk) {
  // Ensure the summarized string is <= 100 chars.
  s = blk.s
  s = s.replaceAll(/\n/, '␤')
  s = s.replaceAll(/\r/, '␍')
  s = s.replaceAll(/\s/, '▪')
  Integer l = s.size()
  String shortS = (l > 60)
    ? "⦗${s.substring(0, 29)}⦘${redEllipse()}⦗${s.substring(l - 29, l - 1)}⦘"
    : s
  return "[t: ${blk.t}, s: ${shortS}]"
}

void showBlocks(ArrayList blocks) {
  paragraph(blocks
    ? blocks.withIndex().collect { blk, i ->
      "blk #${i}: ${glimpseBlock(blk)}"
    }.join('<br>')
    : 'showBlocks() received null argument'
  )
}

void isolatePassthrough(String s) {
  Matcher m = getMatcher('PassthroughRange')
  m.reset(s)
  paragraph("s: >${s}<")
  ArrayList sBlks = []
  Integer lStart = 0
  String carryover
  while (m.find()) {
    if (m.group(2)) {
      // Passthrough RANGE Body
      String stashIndex = now()
      log.info("Stashing >${m.group(2)}< at ${stashIndex}")
      BLOCK_STASH[stashIndex] = m.group(2)
      String found = BLOCK_STASH[stashIndex]
      log.info("Peeking >${found}< RANGE at ${stashIndex}")
      sBlks << s.substring(lStart, m.start(1))
      sBlks << "{{${stashIndex}}}"
      lStart = m.end()
      carryover = s.substring(m.end(), s.size())
    } else if (m.group(6)) {
      // Passthrough PARAGRAPH Body
      String stashIndex = now()
      log.info("Stashing >${m.group(6)}< at ${stashIndex}")
      BLOCK_STASH[stashIndex] = m.group(6)
      String found = BLOCK_STASH[stashIndex]
      log.info("Peeking >${found}< PARA at ${stashIndex}")
      sBlks << s.substring(lStart, m.start(4))
      sBlks << "{{${stashIndex}}}"
      lStart = m.end()
      carryover = s.substring(m.end(), s.size())
    } else {
      log.error('UNEXPECTED')
    }
  }
  sBlks << carryover
  String s2 = sBlks.join()
  paragraph("s2: >${s2}<")
  ArrayList revivedBlks = []
  m = getMatcher('RestoreRef')
  m.reset(s2)
  lStart = 0
  while (m.find()) {
    revivedBlks << s2.substring(lStart, m.start(1))
    revivedBlks << "<div>${BLOCK_STASH[m.group(2)]}</div>"
    lStart = m.end()
    carryover = s2.substring(m.end(), s2.size())
  }
  revivedBlks << carryover
  String s3 = revivedBlks.join()
  paragraph("s3: >${s3}<")
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

String replaceTrivial(String s) {
  return s.replaceAll(/</, '&lt;')
          .replaceAll(/>/, '&gt;')
          .replaceAll(/&/, '&amp;')
          .replaceAll(/\(C\)/, '©')
          .replaceAll(/\(R\)/, '®')
          .replaceAll(/\(TM\)/, '™')
          .replaceAll(/--/, '—')
          .replaceAll(/\.\.\./, '…')
          .replaceAll(/->/, '→')
          .replaceAll(/<-/, '←')
          .replaceAll(/(?s)(\ +\n)/, ' ')
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

SECOND EXAMPLE: content with only select substitutions applied: pass:c,a[__<{John.Smith@google.com}>__]

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
  Example 1: <
  Example 2: >
  Example 3: &
  Example 4: (C)
  Example 5: (R)
  Example 6: (TM)
  Example 7: --
  Example 8: ...
  Example 9: ->
  Example 10: <-

Let's test a few inline references. First a true string: >s1<. Next a list: >list1<.
Nest a sample map: >map1<. That's it for now.

TIP: This is a tip.

IMPORTANT: This is important.

WARNING: This is a warning.

CAUTION: This is a caution.
'''
}
