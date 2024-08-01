@Field static Closure fmtVariable = { def v, String name = null ->
  String r = name ? "${name}: ${v}" : "${v}"
  switch (getObjectClassName(${v})) {
    case 'java.lang.String':
      r += "<b>${v}</b>"
      break
    case 'java.util.ArrayList':
      ArrayList sL = []
      v.each{ e -> sL << "<em>${e}</em>" }
      r += "[${sL.join(', ')}]"
      break
    case 'java.util.LinkedHashMap':
      ArrayList sM = []
      v.each { vk, vv -> sM << "<em>${vk}</em>: <b>${vv}</b>" }
      r += "[${sM.join(', ')}]"
      break
    default:
      r += "<b>${v}</b>" // <em>(${getObjectClassName(v)})</em>"
  }
  return r
}
@Field static Closure dispVariable = { Matcher m ->
  String result = '>>>dispVariable() ERROR<<<'
  if (m.group(2)) {
    def x = m.group(2)
    result = "${x}"  // (${x.getClass()})
  }
  return result
}


String retrieveAndFormatX(def v, String name = null) {
  // Abstract
  //   Eventually: Might be better to have per-type signatures.
  //   For now, this accommodates unknown/unexpected types.
  String prefix = name ? "${name}: " : ''
  String vClassName = getObjectClassName(${v})

  switch (getObjectClassName(varValue)) {
    case 'java.lang.String':
      r += "<b>${varValue}</b>"
      break
    case 'java.util.ArrayList':
      ArrayList sL = []
      varValue.each{ e -> sL << "<em>${e}</em>" }
      r += "[${sL.join(', ')}]"
      break
    case 'java.util.LinkedHashMap':
      ArrayList sM = []
      varValue.each { vk, vv -> sM << "<em>${vk}</em>: <b>${vv}</b>" }
      r += "[${sM.join(', ')}]"
      break
    default:
      r += "<b>${varValue}</b> <em>(${getObjectClassName(varValue)})</em>"
  }
  return r
}

String expandVariables(String sIn) {
  Matcher m = Pattern.compile(/(\$)((\$)|([^\s\p{Punct}]+))/).matcher(sIn)
  //ArrayList outBlks = []
  //Integer lStart = 0
  //String carryover
  while (m.find()) {
    if (m.group(2) == '$') {
      paragraph("expandVariables() found '\$'.")
    } else {
      String varName = m.group(2)
      paragraph("varName: '${varName}'")
      String varValue = "${varName}"
      paragraph("varValue: '${varValue}'")
    }
    //outBlks << sIn.substring(lStart, m.start(1))
    //outBlks << fmtVariable(m.group(2))
    //lStart = m.end()
    //carryover = sIn.substring(m.end(), sIn.size())
  }
  //outBlks << carryover
  return null //outBlks.join()
}

