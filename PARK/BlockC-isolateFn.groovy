ArrayList applyTargetToBlock(Map blk, String parserName) {
  Matcher m = getMatcher(parserName)
  // VERY IMPORTANT:
  //   if (m != null) { ... } - TRUE if a Matcher exists.
  //          if (m) { ... }" - TRUE if current string/index matches pattern.
  if (m != null) {  // NOTE: if (m) { ... } returns
    m.reset(blk.s)
    ArrayList newBlks = []
    // Inside find() loops, 'carryover' is managed by the parser?!
    // On exiting find(), a final raw 'carryover' block is appended to newBlks.
    String carryover = ''
    Integer l = 0
    Integer lStart = 0
    // TEMPORARILY PUT A LOW MAX ON LOOPS TO AVOID RUNAWAY (BAD) MATCHING
    while ((l < 10) && m.find()) {
      paragraph("Find Loop: lStart: ${lStart}, m.regionStart(): ${m.regionStart()}")
      l++
      switch (parserName) {
        case 'Linebreak':
          // Consumes (\+$^) with nothing
          newBlks << [ t: blk.t, s: blk.s.substring(lStart, m.start(1)) ]
          newBlks << [ t: 'Final', s: "<br>" ]  // HTML; so, "<br>" vs '<br>'.
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
          //paragraph("Heading1 w/ newBlks: ${newBlks}")
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
      //-----> carryover = blk.s.substring(m.end(), m.regionEnd())
      lStart = m.end()
    }
    if (newBlks) {
      newBlks << [ t: blk.t, s: carryover ]
      paragraph([
        "matchAndReplace() ${i}.${j}[${blk.t}].${k}[${parserName}] END",
        '-------------------------------------',
        '    N E T   N E W   B L O C K S',
        '-------------------------------------',
        showBlocks(newBlks),
        '-------------------------------------'
      ].join('<br/>'))
      // Replace the single block at blocks[j] with *newBlks.

      replaceBlock(blks, j, newBlks)
      paragraph([
        "matchAndReplace() ${i}.${j}[${blk.t}].${k}[${parserName}] END",
        '-------------------------------------',
        '      R E V I S E D   B L K S',
        '-------------------------------------',
        showBlocks(newBlks),
        '-------------------------------------'
      ].join('<br/>'))
    } else {
      // Give no replacement blocks, mark blocks[j] as Final
      blks[j].t = 'Final'
      paragraph([
        "matchAndReplace() ${i}.${j}[${blk.t}].${k}[${parserName}] END",
        '-------------------------------------',
        '        F I N A L   B L O C K',
        '-------------------------------------',
        blk.s,
        '-------------------------------------'
      ].join('<br/>'))
    }
  } else {
    paragraph("Unable to retrieve Matcher for ${parserName}")
  }
}
