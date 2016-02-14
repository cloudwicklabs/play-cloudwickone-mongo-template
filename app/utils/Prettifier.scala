package utils

object Prettifier {
  /**
    * Counts how many tabs there are at the beginning of the first line and
    * removes this number of tabs at the beginning of every line. After, it
    * replaces all tabs by two spaces.
    */
  def prettify(str: String): String = {
    val i = str.indexOf('\n') + 1
    var n = 0
    while (str.charAt(i + n) == '\t') n += 1
    str.replaceAll(s"\n\t{$n}", "\n").replaceAll("\t", "  ").trim
  }
}
