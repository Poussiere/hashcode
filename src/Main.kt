import com.google.gson.Gson
import java.io.File
import java.lang.Integer.max
import java.lang.Integer.min

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
                    if (j>=0){
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



    fun score(slide1: Slide, slide2: Slide): Int {
        var a = slide1.tags().minus(slide2.tags()).size
        var b = slide2.tags().minus(slide1.tags()).size
        var c = slide1.tags().intersect(slide2.tags()).size
        return minOf(a,b,c)
    }

    //Somme des tags par photos

    fun algo1(dataIn: DataIn) : DataOut {

        val list = mutableListOf<Slide>()
        for (pic in dataIn.photo)
        {
            if (pic.orientation) {
                list.add(Slide(pic))
            }
        }

        return DataOut(list)
    }

    fun algo2(dataIn: DataIn) : DataOut {

        val list = mutableListOf<Slide>()
        var temp : Picture? = null
        for (pic in dataIn.photo)
        {
            if (pic.orientation) {
                list.add(Slide(pic))
            }
            else if (temp != null) {
                list.add(Slide(temp,pic))
                temp = null
            }
            else {
                temp = pic
            }
        }

        return DataOut(list)
    }

    fun algo(dataIn: DataIn) : DataOut {

        val tempList = mutableListOf<Slide>()
        val finalList = mutableListOf<Slide>()
        var temp : Picture? = null
        for (pic in dataIn.photo.sortedByDescending { it.tags.size })
        {
            if (pic.orientation) {
                tempList.add(Slide(pic))
            }
            else if (temp != null) {
                tempList.add(Slide(temp!!,pic))
                temp = null
            }
            else {
                temp = pic
            }
        }
        tempList.sortByDescending { it.tags().size }

        var count = tempList.size / 1000

        var i=0
        while (i< count) {
            var truncateList = tempList.take(min(1000,tempList.size)).toMutableList()
            tempList.removeAll(truncateList)

            var previous = truncateList.first()
            truncateList.remove(previous)
            while (truncateList.isNotEmpty()){
                finalList.add(previous)
                previous = truncateList.maxBy { score(it, previous) }!!
                truncateList.remove(previous)
            }
            finalList.add(previous)

            println(i++)

        }

        return DataOut(finalList)
    }

    fun score(dataOut: DataOut) : Int {
        if (dataOut.slideshow.size<2) {
            return 0
        }
        var previous: Slide? = null
        var total = 0
        for (slide in dataOut.slideshow) {
            if (previous != null) {
                total += score(previous!!, slide)
            }
            previous = slide
        }
        return total
    }

    fun parseOut(dataOut: DataOut): String {
        return "${dataOut.slideshow.size}\n" + dataOut.slideshow.joinToString("\n") {
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