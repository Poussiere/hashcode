import com.google.gson.Gson
import sun.rmi.runtime.Log
import java.io.File

fun main (args : Array<String>){

    val gson = Gson()

    fun  parseIn (input: String): DataIn{
        val inputFile = File(input)
        val tabText = inputFile.readLines(Charsets.UTF_8)
        val pictureCount = tabText[0]
        val listOfPicture = arrayListOf<Picture>()
        val listOfTag = hashMapOf<String,Tag>()
        var i= -1

        //Pictures
        var id = 0
        for (item in tabText){
            if (i>=0){
                val tabLine = item.split(" ")

                //true = vertical, false = horizontal
                var orientation = false
                if (tabLine[0]=="H"){
                    orientation = true
                }

                var tags = arrayListOf<Int>()
                var j = -2

                val picture = Picture (i,
                    orientation,
                    tags
                )
                listOfPicture.add(picture)

                //Tags
                for (item in tabLine){
                    if (j>0){
                        var tag = listOfTag.get(item)
                        if (tag == null) {
                            tag = Tag(id++, arrayListOf(), item)
                            listOfTag[item] = tag
                        }
                        tag.pictures.add(i)
                        tags.add(tag.id)
                    }
                    j++
                }

                // Chercher si le tag existe déjà


            }
            i++
        }

        return DataIn(listOfPicture, listOfTag.values.toList())
    }



    //Somme des tags par photos

    fun algo(dataIn: DataIn) : DataOut {

        val list = mutableListOf<Slide>()
        for (pic in dataIn.photo)
        {
            if (pic.orientation) {
                list.add(Slide(pic))
            }
        }

        return DataOut(list)
    }

    fun score(slide1: Slide, slide2: Slide): Int {
        var a = slide1.tags().minus(slide2.tags()).size
        var b = slide2.tags().minus(slide1.tags()).size
        var c = slide1.tags().intersect(slide2.tags()).size
        return minOf(a,b,c)
    }

    fun score(dataOut: DataOut) : Int {
        if (dataOut.slideshow.size<2) {
            return 0
        }
        var previous: Slide? = null
        var total = 0
        for (slide in dataOut.slideshow) {
            if (previous != null) {
                total += score(previous, slide)
            }
            previous = slide
        }
        return total
    }

    fun parseOut(dataOut: DataOut): String {
        return dataOut.slideshow.joinToString("\n") {
            "${it.pic1.id} ${it.pic2?.id?:""}".trim()
        }
    }

    val datain: DataIn
    if (args[0].contains(".txt")) {
        datain = parseIn(args[0])
        var outputFile = File(args[0].replace(".txt",".json"))
        outputFile.delete()
        outputFile = File(args[0].replace(".txt",".json"))
        outputFile.appendText(gson.toJson(datain))
    }
    else {
        val inputFile = File(args[0])
        datain = gson.fromJson(inputFile.readText(),DataIn::class.java)
    }

    val out = algo(datain)

    var outputFile = File(args[0].split(".")[0] + ".out")
    outputFile.delete()
    outputFile = File(args[0].split(".")[0] + ".out")

    println("Score = " + score(out))

    outputFile.appendText(parseOut(out))

}

data class DataIn (
    var photo: ArrayList<Picture>,
    var tags: List<Tag>
)

data class Picture (
    var id: Int,
    var orientation: Boolean,
    var tags: ArrayList<Int>,
    var selected: Boolean = false
)
data class Tag(
    var id: Int,
    var pictures: ArrayList<Int>,
    var tag: String
)
data class Slide(
    val pic1: Picture,
    val pic2: Picture? = null

) {
    fun tags() : Set<Int> {
        return pic1.tags.toSet().union(pic2?.tags?: listOf())
    }
}





data class DataOut(
    val slideshow: List<Slide>
)