package custom

import custom.Fio.FileSelectUtil
import javax.swing.JButton
import javax.swing.text.JTextComponent

/**
 * @author Straw
 * @since 2023/8/1
 */
class StageExporter {

	val writeData = ArrayList<Array<String>>()

	/**
	 * parse arr to string array and add to writeData
	 */
	private fun addStringArrayLine(arr: Array<Any?>) {
		fun String.parsed(): String {
			if (this.contains("/")) {
				return this.substringAfter("/")
			}
			return this
		}

		val stringData = arr.filterNotNull().map {
			when (it) {
				is JTextComponent -> {
					it.text.parsed()
				}

				is JButton -> {
					it.text.parsed()
				}

				is String -> {
					it
				}

				is Int -> {
					it.toString()
				}

				else -> {
					throw NotImplementedError(it::class.java.name)
				}
			}
		}

		writeData.add(stringData.toTypedArray())
	}


	fun addLine1(arr: Array<Any?>) {
		addStringArrayLine(arr)
	}


	fun addLine2(arr: Array<Any?>) {
		addStringArrayLine(arr)
	}

	fun addLine3List(arr: List<Array<Any?>>) {
		arr.forEach { addStringArrayLine(it) }
	}

	fun toFile() {
		val file = FileSelectUtil.getFile() ?: return
		file.outputStream().bufferedWriter().use {
			for (line in this.writeData) {
				it.write(line.joinToString(",", postfix = "\n"))
			}

			for (i in 0 until  3) {
				it.write("0,0,0,0,0,0,0,9,0,100\n")
			}
		}
	}


}
