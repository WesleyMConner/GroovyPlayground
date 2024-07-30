
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

ArrayList replaceBlock(ArrayList blocklist, Integer pos, ArrayList replacement) {
  Integer blocksIn = blocklist.size()
  Integer replacementBlocks = replacement.size()
  blocklist[pos] = replacement
  ArrayList revisedBlocks = blocklist.flatten()
  Integer blocksOut = revisedBlocks.size()
  paragraph([
    "###############",
    'replaceBlock()',
    "blocksIn: ${blocksIn}",
    "replacementBlocks: ${replacementBlocks}",
    "pos: ${pos}",
    "blocksOut: ${blocksOut}",
    "###############"
  ].join('<br>'))
}

