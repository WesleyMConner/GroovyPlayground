
// ---------------------------------------------------------------------------------
// H ( U B I T A T )   U ( S E R )   I ( N T E R F A C E )
//
//  Copyright (C) 2023-Present Wesley M. Conner
//
// Licensed under the Apache License, Version 2.0 (aka Apache-2.0, the
// "License"); you may not use this file except in compliance with the
// License. You may obtain a copy of the License at
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ---------------------------------------------------------------------------------
// IMPORTS THAT CLIENTS SHOULD ADD:
//   - import com.hubitat.app.ChildDeviceWrapper as ChildDevW
//   - import com.hubitat.app.DeviceWrapper as DevW
//   - import com.hubitat.app.InstalledAppWrapper as InstAppW
//   - import com.hubitat.hub.domain.Event as Event
//   - import groovy.json.JsonOutput as JsonOutput
//   - import groovy.json.JsonSlurper as JsonSlurper
//   - import groovy.transform.Field
//   - import java.lang.Math as Math
//   - import java.lang.Object as Object
//   - import java.util.concurrent.ConcurrentHashMap

/* groovylint-disable-next-line MethodCount */
@Field static ConcurrentHashMap<String, String> HUED_CACHE = [:]

library(
 name: 'WmcUtilsLib_1_0_0',
 namespace: 'Wmc',
 author: 'WesleyMConner',
 description: 'Methods leveraged generally across other libraries and Apps',
 category: 'general purpose',
 documentationLink: 'TBD',
 importUrl: 'TBD'
)

// HEADERS
String heading(String s, size, font) {
  return "<span style='font-size: ${size}; font-family: ${font};'>${s}</span>"
}
String h1(String s) { return heading(s, '2em', 'Roboto') }
String h2(String s) { return heading(s, '1.3em', 'Roboto') }
String h3(String s) { return heading(s, '1.1em', 'Roboto') }

// BULLETS
String bullet(String s, String bullet, Integer before, Integer after) {
  return "${'&nbsp;' * before}${bullet}${'&nbsp;' * after}${s}"
}
String bullet1(String s) { return bullet(s, '&#x25CF;', 0, 2) } // Was: x2022
String bullet2(String s) { return bullet(s, '&#x25CF;', 2, 2) }
String bullet3(String s) { return bullet(s, '&#x25CF;', 4, 2) }
String square1(String s) { return bullet(s, '&#x25FC;', 0, 2) }
String square2(String s) { return bullet(s, '&#x25FC;', 2, 2) }
String square3(String s) { return bullet(s, '&#x25FC;', 4, 2) }

// TEXT EMPHASIS

String b(def val) {
  String retVal = '<b>null</b>'
  if (val == '0') {
    retVal = '<b>0</b>'
  } else if (val == 0) {
    retVal = '<b>0</b>'
  } else if (val) {
    retVal = "<b>${val}</b>"
  }
  return retVal
}

String i(def val) { return val ? "<i>${val}</i>" : '<i>null</i>' }

String bi(def val) { return i(b(val)) }

String bList(ArrayList list) {
  return '[' + list.inject([]) { s, e -> s << "${b(e)} " }.join(', ') + ']'
}

String bMap(Map map) {
  ArrayList terms = []
  map.each { k, v -> terms.add("${i(k)}: ${b(v)}") }
  return '[' + terms.join(', ') + ']'
}

// MAP TO TABLE

String mapToTable(Map m, Boolean borders = true) {
  ArrayList t = [ borders ? '<table border="1">' : '<table>' ]
  m.each { k, v ->
    t << "<tr><td align='right'>${i(k)}${borders ? '' : ':'}</td>"
    t << "<td>${b(v)}</td></tr>"
  }
  t << '</table>'
  return t.join()
}

String eventDetails(Event e) {
  logInfo('eventDetails', ['',
    "getObjectClassName(e): ${getObjectClassName(e)}",
    "e: ${e}",
    "e: ${e as Map}"
  ])
  return mapToTable(e)
}

// CONVENIENCE METHODS

Integer safeParseInt(String s) {
  Integer result = null
  switch (s) {
    case null:
    case '':
      break
    default:
      result = s.toInteger()
  }
  return result
}

ArrayList cleanStrings(ArrayList list) {
  // Prune nulls, empty strings and dups
  return list.findAll { s -> s ?: null }.unique()
}

ArrayList modeNames() {
  return getLocation().getModes()*.name
}

String toJson(def thing) {
  def output = new JsonOutput()
  return output.toJson(thing)
}

// HTML COLOR BARS
String blackBar() { return '<hr style="border: 5px solid black;"/>' }
String greenBar() { return '<hr style="border: 5px solid green;"/>' }
String redBar() { return '<hr style="border: 5px solid red;"/>' }

// HTML ALERT BOX
String alert(String s, String bgcolor='#FFFF8F', String border='2px') {
  return [
    '<span style="display:inline-table;"><table><tr>',
    "<td style='border: solid ${border} black; background-color: ${bgcolor};'>${b(s)}</td>",
    '</tr></table></span>'
  ].join()
}

// INDIVIDUALLY BORDERED TABLE CELL
String tdBordered(def content) {
  return "<td style='border: solid 1px black;'>${content}</td>"
}

// FOREGROUND AND BACKGROUND HEX COLORS FOR hued()

Map getFgBg() {
  return [
    '-39': ['#FF8000', '#FFFFFF'], '-38': ['#FF1493', '#FFFFFF'],
    '-37': ['#FF00FF', '#FFFFFF'], '-36': ['#FF0000', '#FFFFFF'],
    '-35': ['#DB7093', '#FFFFFF'], '-34': ['#DAA520', '#FFFFFF'],
    '-33': ['#DA70D6', '#FFFFFF'], '-32': ['#D2691E', '#FFFFFF'],
    '-31': ['#CF8500', '#FFFFFF'], '-30': ['#CD5C5C', '#FFFFFF'],
    '-29': ['#C71585', '#FFFFFF'], '-28': ['#B8560B', '#FFFFFF'],
    '-27': ['#9400D3', '#FFFFFF'], '-26': ['#9370DB', '#FFFFFF'],
    '-25': ['#8B4513', '#FFFFFF'], '-24': ['#8B008B', '#FFFFFF'],
    '-23': ['#808000', '#FFFFFF'], '-22': ['#800000', '#FFFFFF'],
    '-21': ['#7B68EE', '#FFFFFF'], '-20': ['#708090', '#FFFFFF'],
    '-19': ['#6B8E23', '#FFFFFF'], '-18': ['#696969', '#FFFFFF'],
    '-17': ['#6495ED', '#FFFFFF'], '-16': ['#4B0082', '#FFFFFF'],
    '-15': ['#4682B4', '#FFFFFF'], '-14': ['#4169E1', '#FFFFFF'],
    '-13': ['#32CD32', '#FFFFFF'], '-12': ['#228B22', '#FFFFFF'],
    '-11': ['#20B2AA', '#FFFFFF'], '-10': ['#1E90FF', '#FFFFFF'],
     '-9': ['#00CED1', '#FFFFFF'],  '-8': ['#00BFFF', '#FFFFFF'],
     '-7': ['#008080', '#FFFFFF'],  '-6': ['#0000FF', '#FFFFFF'],
     '-5': ['#FFFFFF', '#C05000'],  '-4': ['#FFEA00', '#C05000'],
     '-3': ['#10DEE1', '#C05000'],  '-2': ['#00FF00', '#C05000'],
     '-1': ['#FFFFFF', '#9400D3'],   '0': ['#FFEA00', '#9400D3'],
      '1': ['#FFA000', '#9400D3'],   '2': ['#10DEE1', '#9400D3'],
      '3': ['#00FF00', '#9400D3'],   '4': ['#FFFFFF', '#606000'],
      '5': ['#FFEA00', '#606000'],   '6': ['#FFA000', '#606000'],
      '7': ['#10DEE1', '#606000'],   '8': ['#00FF00', '#606000'],
      '9': ['#FFFFFF', '#800000'],  '10': ['#FFEA00', '#800000'],
     '11': ['#FFA000', '#800000'],  '12': ['#FF8888', '#800000'],
     '13': ['#10DEE1', '#800000'],  '14': ['#00FF00', '#800000'],
     '15': ['#FFFFFF', '#4B0082'],  '16': ['#FFEA00', '#4B0082'],
     '17': ['#FFA000', '#4B0082'],  '18': ['#FF8888', '#4B0082'],
     '19': ['#FF34B3', '#4B0082'],  '20': ['#10DEE1', '#4B0082'],
     '21': ['#00FF00', '#4B0082'],  '22': ['#FFFFFF', '#006060'],
     '23': ['#FFEA00', '#006060'],  '24': ['#FFA000', '#006060'],
     '25': ['#FF8888', '#006060'],  '26': ['#10DEE1', '#006060'],
     '27': ['#00FF00', '#006060'],  '28': ['#FFFFFF', '#0000FF'],
     '29': ['#FFEA00', '#0000FF'],  '30': ['#FF34B3', '#0000FF'],
     '31': ['#20EEF1', '#0000FF'],  '32': ['#20FF20', '#0000FF'],
     '33': ['#FFFFFF', '#000000'],  '34': ['#FFEA00', '#000000'],
     '35': ['#FFA000', '#000000'],  '36': ['#FF8888', '#000000'],
     '37': ['#FF34B3', '#000000'],  '38': ['#10DEE1', '#000000'],
     '39': ['#00FF00', '#000000']
  ]
}

ArrayList getHuedCacheContents() {
  return [
    b('HUED_CACHE CONTENTS:'),
    '-- begin --',
    *(HUED_CACHE.collect { k, v ->
      "${k} → ${v}, stripHued(..): '${stripHued(v)}'"
    }),
    '-- end --'
  ]
}

String getFgBgTable() {
  ArrayList html = ['<table rules="all">']
  html << '<tr><th>Index</th><th>FG</th><th>BG</th><th>Sample</th></tr>'
  getFgBg().each { k, fsBg ->
    String fg = fsBg[0]
    String bg = fsBg[1]
    html << "<tr><td align='center'>${k}</td><td>${fg}</td><td>${bg}</td>"
    html << "<td style='color: ${fg}; background-color: ${bg};'>WmcLengthyName</td></tr>"
  }
  html << '</table>'
  return html.join()
}

// Offer hued w/ several signatures

String hued(InstAppW a) {
  String result = 'hued_ERROR'
  if (a) {
    result = hued(a.getLabel(), a?.id)
  } else {
    logError('hued(InstAppW)', 'Called with null argument')
  }
  return result
}

String hued(ChildDevW d) {
  String result = 'hued_ERROR'
  if (d) {
    result = hued(d.getDeviceNetworkId(), (d.id as Long))
  } else {
    logError('hued(ChildDevW)', 'Called with null argument')
  }
  return result
}

String hued(DevW d) {
  String result = 'hued_ERROR'
  if (d) {
    result = hued(d.getDeviceNetworkId(), (d.id as Long))
  } else {
    logError('hued(DevW)', 'Called with null argument')
  }
  return result
}

String hued(Event e) {
  return hued(e.displayName, e.deviceId)
}

String hued(String sArg = null, Long iArg = null) {
  //String s = sArg ?: app?.getLabel() ?: device?.getDeviceNetworkId()
  String s = sArg ?: app?.getLabel() ?: app?.getName()?.toString() ?: device?.getDeviceNetworkId()
  Long i = iArg ?: app?.id ?: (device?.id as Long)
  String key = "${s}&#x25FC;${i}"
  String ss = (s && s.length() > 15 ) ? "…${s.substring(s.length() - 15, s.length())}" : s
  return [
    "sArg: ${sArg}",
    "app?.getLabel(): ${app?.getLabel()}",
    "app?.getName()?.toString(): ${app?.getName()?.toString()}",
    "device?.getDeviceNetworkId(): ${device?.getDeviceNetworkId()}",
    "iArg: ${iArg}",
    "app?.id: ${app?.id}",
    "device?.id as Long: ${device?.id as Long}",
    "s: ${s}",
    "i: ${i}",
    "key: ${key}",
    "ss: ${ss}"
  ].join('<br/>')
//logDebug('hued', "key: ${key}")
  String fLabel = HUED_CACHE[key]
  if (!fLabel) {
    String index = "${Math.round(s.hashCode() / i) % 39}"
    ArrayList fgBg = getFgBg()[index]
    if (fgBg) {
      fLabel = [
        "<span style='color: ${fgBg[0]}; background-color: ${fgBg[1]};'>",
        "…${ss}",
        '</span>'
      ].join()
      HUED_CACHE[key] = fLabel
    } else {
      logError(
        'hued',
        "Cache refresh failed for key: ${b(key)} and index: ${b(index)}."
      )
    }
  }
  return fLabel
}

String stripHued(String html) {
  return html?.replaceAll(/<\/?[^>]*>/, '')
}

void setLogLevel(String logThreshold) {
  Map logThresholdToLogLevel = [ TRACE: 5, DEBUG: 4, INFO: 3, WARN: 2, ERROR: 1 ]
  atomicState['logLevel'] = logThresholdToLogLevel."${logThreshold}" ?: 'TRACE'
}

Boolean ifLogTrace() { return (state.logLevel ?: 5) > 4 }
Boolean ifLogDebug() { return (state.logLevel ?: 5) > 3 }
Boolean ifLogInfo()  { return (state.logLevel ?: 5) > 2 }
Boolean ifLogWarn()  { return (state.logLevel ?: 5) > 1 }

void logTrace(String fnName, String s) {
  ifLogTrace() && log.trace("${hued()}.<b>${fnName}⟮ ⟯</b> → ${s} [${getObjectClassName(this)}]")
}

void logTrace(String fnName, ArrayList ls, String delim = '<br/>&nbsp&nbsp') {
  logTrace(fnName, ls.join(delim))
}

void logDebug(String fnName, String s) {
  ifLogDebug() && log.debug("${hued()}.<b>${fnName}⟮ ⟯</b> → ${s} [${getObjectClassName(this)}]")
}

void logDebug(String fnName, ArrayList ls, String delim = '<br/>&nbsp&nbsp') {
  logDebug(fnName, ls.join(delim))
}

void logInfo(String fnName, String s) {
  ifLogInfo() && log.info("${hued()}.<b>${fnName}⟮ ⟯</b> → ${s} [${getObjectClassName(this)}]")
}

void logInfo(String fnName, ArrayList ls, String delim = '<br/>&nbsp&nbsp') {
  logInfo(fnName, ls.join(delim))
}

void logWarn(String fnName, String s) {
  ifLogWarn() && log.warn("${hued()}.<b>${fnName}⟮ ⟯</b> → ${s} [${getObjectClassName(this)}]")
}

void logWarn(String fnName, ArrayList ls, String delim = '<br/>&nbsp&nbsp') {
  logWarn(fnName, ls.join(delim))
}

void logError(String fnName, String s) {
  // Report all errors (i.e., no conditional test)
  log.error("${hued()}.<b>${fnName}⟮ ⟯</b> → ${s} [${getObjectClassName(this)}]")
}

void logError(String fnName, ArrayList ls, String delim = '<br/>&nbsp&nbsp') {
  logError(fnName, ls.join(delim))
}
