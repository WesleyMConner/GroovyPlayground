// ----------------------------------------------------------------------------
def stashContent = { Matcher m ->
  String result = '>>>stashContent() ERROR<<<'
  if (m.group(2)) {
    String stashKey = now()
    BLOCK_STASH[stashKey] = m.group(2)
    result = "{{${stashKey}}}"
  }
  return result
}

def retrieveContent = { Matcher m ->
  return BLOCK_STASH[m.group(2)]
}

def replaceAllWithFn = { String parserName, String sIn, Closure replacerFn ->
  Matcher m = getMatcher(parserName)
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
// ----------------------------------------------------------------------------
