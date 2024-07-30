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

@Field static ConcurrentHashMap<String, Pattern> PATTERNS = [:]
@Field static ConcurrentHashMap<String, Matcher> MATCHERS = [:]

// The Groovy Linter generates NglParseError on Hubitat #include !!!
definition(
  name: 'DemoB',
  namespace: 'Wmc',
  author: 'Wesley M. Conner',
  description: 'Develop AsciiDoc-like parsing',
  singleInstance: true,
  iconUrl: '',
  iconX2Url: ''
)

preferences {
  page(name: 'DemoB')
}

Map DemoB() {
  return dynamicPage(
    name: 'DemoB',
    title: 'DemoB',
    install: true,
    uninstall: true
  ) {
    section {
      Closure fn = { Matcher m ->
        return "${m.groupCount()}" //(1) ?: null}}, g2: ${m?.group(2) ?: null}, g3: ${m?.group(3) ?: null}}"
      }
      //paragraph 'Everything happens on installation. Click "Done".'
      String data = '''This has [yellow-background]#*bold01*# and _italic01_
      and +other01+ over. It has another [red]#wave# of *bold02* and +other02+.'''
      // Bold
      //Matcher m = Pattern.compile(/(?mg)(?!^[\*]).*?\*(.*?)\*/).matcher('')
      //Matcher m = Pattern.compile(/\*(.*?)\*/).matcher('')  // Weak BOLD
      Matcher m = Pattern.compile(/(?s)\[([^\]]*)\]\#([^#]*?)\#/).matcher('')  // FOREGROUND
      m.reset(data)
      while (m.find()) {
        paragraph(fn(m))
      }

      //expander(data, getDataMatcher(), '<b>G1</b>')
      //expander(data, getDataMatcher(), '<em>G1</em>')
      //expander(data, getDataMatcher(), 'G1 and G2')
    }
  }
}



Matcher getDataMatcher() {
  Pattern p = Pattern.compile(/\+(.*?)\+/)
  Matcher m = p.matcher('')
  return m
}

String expander(String s, Matcher m, String replacer) {
  paragraph("#68: s: ${s}, ")
  m.reset(s)
  ArrayList result = []
  Integer iterationStart = 0
  String carryover
  while (m.find()) {
    result << s.substring(iterationStart, m.start())
    paragraph("#75: result: ${result}")
    String r = replacer
    if (m?.group(1)) { r.replaceAll('G1', m.group(1)) }
    paragraph("#78: result: ${result}")
    //if (m?.group(2)) { r.replaceAll('G2', m.group(2)) }
    //if (m?.group(3)) { r.replaceAll('G3', m.group(3)) }
    result << r
    paragraph("#82: result: ${result}")
    carryover = s.substring(m.end(), m.regionEnd())
  }
  result << carryover
  paragraph("#86: result: ${result.join('<br/>')}")
  return result.join('<br/>')
}

// CORE METHODS

String replaceTrivial(String s) {
  return s.replaceAll(/\(C\)/, '©')
          .replaceAll(/\(R\)/, '®')
          .replaceAll(/\(TM\)/, '™')
          .replaceAll(/--/, '—')
          .replaceAll(/\.\.\./, '…')
          .replaceAll(/->/, '→')
          .replaceAll(/<-/, '←')
}

void populatePatternAndMatcherCaches() {
  // Abstract
  //   Realize a subset of AsciiDoc with tweaks for Groovy GStrings and
  //   key data types. [See https://powerman.name/doc/asciidoc.html]
  //   Prebuild the compilers up front as a one-time cost.
  //   Prebuild per-compiler matchers - using matcher.reset(String s)
  //   to process a new String.
  //
  //   (?m) - facilitates use of ^ and $ .. ADJUSTING text as needed
  //   (?s) - .* will also match ^ and $
  //   (?ms) - Applies both of the above

  /*
  Pattern p = Pattern.compile(/ /)
  PATTERNS['Level1'] = p
  MATCHERS['Level1'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['Level2'] = p
  MATCHERS['Level2'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['Level3'] = p
  MATCHERS['Level3'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['ParaTitle'] = p
  MATCHERS['ParaTitle'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['Para'] = p
  MATCHERS['Para'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['Literal'] = p
  MATCHERS['Literal'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['Note'] = p
  MATCHERS['Note'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['Tip'] = p
  MATCHERS['Tip'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['Important'] = p
  MATCHERS['Important'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['Warning'] = p
  MATCHERS['Warning'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['Caution'] = p
  MATCHERS['Caution'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['ListingBlock'] = p
  MATCHERS['ListingBlock'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['SidebarBlock'] = p
  MATCHERS['SidebarBlock'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['ExampleBlock'] = p
  MATCHERS['ExampleBlock'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['LiteralBlock'] = p
  MATCHERS['LiteralBlock'] = p.matcher('')

  p = Pattern.compile(/ /)
  PATTERNS['QuoteBlock'] = p
  MATCHERS['QuoteBlock'] = p.matcher('')

  // LEAF
  p = Pattern.compile(/ /)
  PATTERNS['AsIs'] = p
  MATCHERS['AsIs'] = p.matcher('')

  // LEAF
  p = Pattern.compile(/ /)
  PATTERNS['LineBreak'] = p
  MATCHERS['LineBreak'] = p.matcher('')
*/
  // LEAF
  p = Pattern.compile(/(?s)_(.*?)_/)
  PATTERNS['Emphasis'] = p
  MATCHERS['Emphasis'] = p.matcher('')

  //Bold needs to exclude asterisk in column 1.
  //p = Pattern.compile(/(?s)\*(.*?)\*/)
  //PATTERNS['Bold'] = p
  //MATCHERS['Bold'] = p.matcher('')
/*

  // LEAF
  p = Pattern.compile(/(?m)^(.*?)+(.*?)+(.*)$/)
  PATTERNS['Mono'] = p
  MATCHERS['Mono'] = p.matcher('')

  // LEAF
  p = Pattern.compile(/(?m)^(.*?)^(.*?)^(.*)$/)
  PATTERNS['Superscript'] = p
  MATCHERS['Superscript'] = p.matcher('')

  // LEAF
  p = Pattern.compile(/(?m)(.*?)+(.*?)+(.*)$/)
  PATTERNS['Subscript'] = p
  MATCHERS['Subscript'] = p.matcher('')

  // LEAF
  PATTERNS['Command'] = p
  MATCHERS['Command'] = p.matcher('')

  // LEAF
  PATTERNS['FG'] = p
  MATCHERS['FG'] = p.matcher('')

  // LEAF
  PATTERNS['BG'] = p
  MATCHERS['BG'] = p.matcher('')

  // LEAF
  PATTERNS['BIG'] = p
  MATCHERS['BIG'] = p.matcher('')

  // LEAF
  PATTERNS['hbar'] = p
  MATCHERS['hbar'] = p.matcher('')

  PATTERNS['bullet1'] = p
  MATCHERS['bullet1'] = p.matcher('')

  PATTERNS['bullet2'] = p
  MATCHERS['bullet2'] = p.matcher('')

  PATTERNS['bullet3'] = p
  MATCHERS['bullet3'] = p.matcher('')

  PATTERNS['termWithDef1'] = p
  MATCHERS['termWithDef1'] = p.matcher('')

  PATTERNS['horzTermWithDef1'] = p
  MATCHERS['horzTermWithDef1'] = p.matcher('')

  PATTERNS['qanda'] = p
  MATCHERS['qanda'] = p.matcher('')

  PATTERNS['table'] = p
  MATCHERS['table'] = p.matcher('')

  PATTERNS['csv'] = p
  MATCHERS['csv'] = p.matcher('')
  */
}

String replaceLeafOccurrences(String sArg) {
  String s = sArg
  ArrayList targets = [ 'Emphasis' /*, 'Bold'*/ ]
  targets.forEach{ target ->
    ArrayList revisedS = []
    //Matcher m = (s =~ p)
    Matcher m = MATCHERS[target]  // Pull prebuilt matcher from cache.
    m.reset(s)                    // Reset the matcher using the current data.
    String carryover = ''
    Integer iterationStart = 0
    while (m.find()) {
      if (m.start() > 0) {
        String before = s.substring(iterationStart, m.start())
        String replace
        switch(target) {
          case 'Emphasis':
            replace = "<em>${m.group(1)}</em>"
            break
          case 'Bold':
            replace = "<b>${m.group(1)}</b>"
            break
          default:
            log.error("replaceLeafOccurrences() cannot replace target ${target}.")

        }
        carryover = s.substring(m.end(), m.regionEnd())
        iterationStart = m.end()
        paragraph([
          "<table border='1'><tr><th>before</th><td>${summarizeString(before)}</td></tr>",
          "<tr><th>replace</th><td>${replace}</td></tr>",
          "<tr><th>carryover</th><td>${summarizeString(carryover)}</td></tr></table>",
        ].join('<br/>'))
        revisedS << before
        revisedS << replace
      }
    }
    revisedS << carryover
    paragraph([
      '--------------------------------------------------',
      "AFTER ${target}:",
      '--------------------------------------------------',
      revisedS.join(),
      '--------------------------------------------------',
    ].join('<br/>'))
    s = revisedS
  }
  return s
}

void processString() {
  // (?m) - facilitates use of ^ and $
  // (?s) - .* will also match ^ and $
  // (?ms) - Applies both of the above
  populatePatternAndMatcherCaches()
  String s = getSampleData()
  s = replaceTrivial(s)
  s = replaceLeafOccurrences(s)
  paragraph s
}

// X * X * X * X * X * X * X * X * X * X * X * X * X * X * X * X * X * X * X

ArrayList replaceBlock(ArrayList blocklist, Integer pos, ArrayList replacement) {
  blocklist[pos] = replacement
  return blocklist.flatten()
}

void processStringXXX() {
  //-> ArrayList z1 = ['a', 'b', 'c', 'd', 'e']
  //-> z1 = replaceBlock(z1, 2, ['c1', 'c2', 'c3'])
  //-> paragraph("z1: ${z1}")

  String s = getSampleData()

  // DO WHOLE-STRING, LEAF-LEVEL INLINE REPLACEMENTS

  //paragraph("s: ${s}")

  //Pattern p = Pattern.compile(/(?m)^(.*?)_(.*?)_(.*)$/) //WORKED
  Pattern p = Pattern.compile(/(?m)^(.*?)_(.*?)_(.*)$/)
  //paragraph("p: ${p}")

  /*
  Matcher m = (s =~ patterns['b'])
  while (m.find()) {
    if (m.start() > 0) {
      paragraph "Before: ${s.substring(0, m.start())}"
    }
    paragraph "Hit: <b>${m.group(1)}</b>"
    if (m.end() != m.regionEnd()) {
      paragraph "After: ${s.substring(m.end(), m.regionEnd())}"
    }
  }
  paragraph "Last Hit?: <b>${m.group(1)}</b>"
  if (m.end() != m.regionEnd()) {
    paragraph "After: ${s.substring(m.end(), m.regionEnd())}"
  }
  */

  //s.replaceAll(/(?m)^(.*?)_(.*?)_(.*)$/, "$1")
  //s.replaceAll(/(.*?)_(.*?)_/, '$1<em>$2</em>')
  s.replaceAll(/(?BEFORE.*?)_(?ITALIC.*?)_/, "XXXXX${BEFORE}XXXXX${ITALIC}XXXXX")
  paragraph "s: >${s}"

  /* *****
  Matcher m = (s =~ p)
  Integer counter = 0
  while (m.find()) {
    ++counter
    if (m.start() > 0) {
      paragraph "${counter}: <b>BEFORE:</b> ${s.substring(0, m.start())}"
    }
    paragraph "<b>HIT:</b> <em>${m.group(1)}</em>"
    if (m.end() != m.regionEnd()) {
      paragraph "<b>AFTER:</b> ${s.substring(m.end(), m.regionEnd())}"
    }
  }
  ***** */
//    paragraph "Last Hit?: <em>${m.group(1)}</em>"
//    if (m.end() != m.regionEnd()) {
//      paragraph "After: ${s.substring(m.end(), m.regionEnd())}"
//    }

  //-> s.eachWithIndex { e, i -> log.info("${i}: >${e}<") }
  // Isolate Lines

  //-> paragraph("s class: ${getObjectClassName(s)}")
  //-> s.eachWithIndex { e, i -> log.info("${i}: >${e}<") }

//  Map patterns = [
//    h3: Pattern.compile(/(?m)^=== (.*)$/),
//    h2: Pattern.compile(/(?m)^== (.*)$/),
//    h1: Pattern.compile(/(?m)^= (.*)$/),
//    asIs: Pattern.compile(/(?m)^( .*)$/),
//    b: Pattern.compile(/(?m)\*.*?\*/),                // For bold, not greed
//    i: Pattern.compile(/(?m)_.*?_/)                   // For italic, not greed
//  ]
  //-> log.info("patterns: ${patterns}")
//  Map blocksToOrderedPatterns = [
//    //'raw': ['h3', 'h2', 'h1', 'b', 'i'],
//    raw: ['b', 'i'],
//    h3: ['b', 'i'],
//    h2: ['b', 'i'],
//    h1: ['b', 'i'],
//    b: [],
//    i: [],
//    asIs: []
//  ]
//  //-> log.info("blocksToOrderedPatterns: ${blocksToOrderedPatterns}")
//  ArrayList blocks = [['raw', s]]
//  showBlocks(blocks)
  //-----
  //Integer outerMax = 1
//  Integer outer = 1
  //logBlockSummary(outer, block)
  //while (outer <= outerMax) {
//    blocks.eachWithIndex { block, i ->
      //==> ArrayList nextBlocks = []
//      String blkType = block[0]
//      String blkData = block[1]
//      ArrayList orderedPatterns = blocksToOrderedPatterns[blkType]

//      replaceBlock(blocks, i, applyPatternNameToBlock(String patternName, ArrayList block))

//      orderedPatterns.each { patternName ->
//        paragraph("===== PROCESSING BLOCK #${i}, PATTERN ${patternName} =====")

//        Pattern pattern = patterns[patternName]
//        Matcher m = (blkData =~ pattern)
        // Instead of looping, do a "one and done"
//        if (m.find()) {
//          if (m.start() > 0) {
//            nextBlocks << ['raw', blkData.substring(0, m.start())]
//          }
//          nextBlocks << [patternName, "<b>${m.group(1)}</b>"]
//          if (m.end() != m.regionEnd()) {
//            nextBlocks << ['raw', blkData.substring(m.end(), m.regionEnd())]
//          }
//        }
//        showBlocks(nextBlocks)
//      }
//    }
  //  outerMax++
  //  outer = outerMax
  //}
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
  s = sArg.replace('\n', '␤')
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


  /*
    // EXAMPLE 8: Adjusted pattern and loop limiter.
    //   log.info("processString at Entry s: ${s}")
    //   Pattern p = Pattern.compile(/X(.*?)X/)
    //   Matcher m = (s =~ p)
    //   Integer maxLoops = 5
    //   while (maxLoops && m.find()) {
    //     log.info("m: ${m}")
    //     maxLoops = maxLoops - 1
    //   }
    //   WORKED (logs; so, order is reversed.)
    //     m: java.util.regex.Matcher[pattern=X(.*?)X region=0,53 lastmatch=Xmore stuffX]
    //     m: java.util.regex.Matcher[pattern=X(.*?)X region=0,53 lastmatch=XboldX]
  */
    // ANALYSIS OF LOOPING
    //   The issue *may* be the full line match - as opposed to
    //   just isolating one element to operate on.
    //==HOLD==>
    //==HOLD==> ArrayList blocks = []
    //==HOLD==>
    //==HOLD==> log.info("processString at Entry s: ${s}")
    //==HOLD==> Pattern p = Pattern.compile(/X(.*?)X/)
    //==HOLD==> Matcher m = (s =~ p)
    //==HOLD==> Integer maxLoops = 5
    //==HOLD==> while (maxLoops && m.find()) {
    //==HOLD==>   log.info("m: ${m}")
    //==HOLD==>   maxLoops = maxLoops - 1
    //==HOLD==> }

// MORE METHODS

void installed() {
  // Called when a bare device is first constructed.
  f()
}

void updated() {
  // Called when a human uses the Hubitat GUI's Device drilldown page to edit
  // preferences (aka settings) AND presses 'Save Preferences'.
  f()
}

void f() {
  log.info('f() has nothing to do for now.')
  /*
  ChildDevW d = getChildDevice('myX')
  if (d) {
    log.info('Using existing myX')
  } else {
    d = addChildDevice(
      'Wmc',   // namespace
      'x',     // typeName
      'myX',   // Device Network Identifier
      [
        isComponent: true,  // Lifecycle is tied to parent
        name: 'myX',
        label: 'myX'
      ]
    )
  }
  */
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

* Monday Tuesday Wednesday *Thursday* Friday
** Apples _Bananas_ Oranges _Grapes_
*** Cats _*Dogs Birds Turtles*_ Rabbits Fish

=== This is `Header 3`
normal paragraph 3

normal paragraph 4 that keeps on `going and going and going` and going and going
and going and going and going and going and ... going and going and going and
going and going.

descriptive list1::
descriptive list information ... one two three four five

descriptive list2::
* list2 bullet 1
* list2 bullet 1
* list2 bullet 1

 This text should be presented 'as is' with no formatting. This text should be
 presented 'as is' with no formatting. This text should be presented 'as is'
 with no formatting. This text should be presented 'as is' with no formatting.
 This text should be presented 'as is' with no formatting. This text should be
 presented 'as is' with no formatting. This text should be presented 'as is'
 with no formatting.

.This acts as a simple header

This is a paragraph that runs for more than one line. Here is a It's just here
to occupy space and flesh-out testing.

.Special Text
Copyright(C),TemporaryMark(TM),Registered(R),EmDash--,Elipse...,
RightArrow->,LeftArrow<-

[  cols = 3 ]
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

Let's test a few inlines strings. First a true string: >s1<. Next a list: >list1<.
Nest a sample map: >map1<. That's it for now.

'''
}