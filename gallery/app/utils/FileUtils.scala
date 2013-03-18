package utils

import java.io.File
import java.io.FileNotFoundException
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import net.coobird.thumbnailator.Thumbnails

object FileUtils {

  def getFile(dir: String, name: String) = isFile(dir, name)
    
  private def isFile(dir: String, name: String) : File = {
    val file = new File(dir + name)
    file.isFile() match {
      case true => file
      case _ => throw new FileNotFoundException()
    }
  }
  
  private def getLastElement(list: List[String]) : String = {
    // list.last :-/
    list match {
      case Nil => throw new NoSuchElementException
      case head :: Nil => head
      case _ :: tail => getLastElement(tail) 
    }
  }
  
  def createThumbnails(dirFrom: String, dirTo: String, filename: String, widthMax: Int, heightMax: Int) {
	  var height = heightMax
    var width = widthMax
    
    val bImage: BufferedImage = ImageIO.read(new File(dirFrom + filename));
    
    if (bImage.getHeight() > heightMax) {
     var factor = bImage.getHeight() / heightMax
     width = bImage.getWidth() / factor
     if (width > widthMax) {
       width = widthMax
       factor = bImage.getWidth() / widthMax
       height = bImage.getHeight() / factor
     }
    } else {
      height = bImage.getHeight()
    }
    
    createThumbnails(
        new File(dirFrom + filename),
        new File(dirTo + filename), width, height)
  }
  
  def createThumbnails(from: File, to:File, width: Int, height: Int) {
    Thumbnails.of(from)
      .size(width, height)
      .outputQuality(1)
      .toFile(to);
  }
  
  def move(from: File, directoryTo: String, filename: String) {
    from.renameTo(new File(directoryTo + filename))
  }
  
  def getFileType(filename: String) : String = {
	  if (filename == null) {
	    throw new IllegalArgumentException("filename of getFileType is null")
	  }

    val tab = filename.split("\\.").toList
	  if (!tab.isEmpty) {
	    return tab.last
	  } else {
	    throw new IllegalArgumentException("filename {" + filename + "} has no type")
	  }
  }
  
  def listFilename(directory: String) : List[String] = {
    val dir = new File(directory)
    var files: List[String] = List.empty
    if (dir.isDirectory()) {
      for (file <- dir.listFiles()) {
        files ::= file.getName()
      }
    }
    return files
  }
  
  def delete(filename: String, dirFrom: String) : Boolean = {
    val file = new File(dirFrom + filename)
    if (!file.isFile()) {
      throw new IllegalArgumentException("File {" + dirFrom + filename + "} is not valid")
    }
    return file.delete()
  }
}