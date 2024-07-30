//        Boolean find() - A match was found
//        String group() - Effectively group(0) the sequence matched
//     String group(int) - Contents of a matching group
//  String group(String) - Contents of a named capturing group
//      Boolean hitEnd() - There are no matches.
// Integer regionStart() - Start of text range being matched.
//   Integer regionEnd() - End of text range being matched.

void processString() {
  String s = getSampleData()
  Matcher m = getMatcher('Heading1')
  m.reset(s)
  String xs = m.replaceAll('''<span style='font-size: 3.0em;'>$2</span>''')
  paragraph(xs)
}