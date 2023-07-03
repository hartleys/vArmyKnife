package internalUtils


import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.File;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import scala.collection.JavaConverters._

//import scala.collection.JavaConversions._
import internalUtils.optionHolder._;
import internalUtils.Reporter._;
import internalUtils.stdUtils._;

import htsjdk.samtools._;
import internalUtils.fileUtils._;

import org.mapdb.DBMaker;
//import net.openhft.chronicle._;

object dbUtils {
  
    
  class DiskFallbackStringSet( setID : String, maxReadCache : Int = 1000000, commitCount : Int = 1000, warnMultiplier : Int = 10) extends scala.collection.mutable.Set[String] {
    val db = DBMaker.fileDB(new File("test.txt")).fileMmapEnableIfSupported().cleanerHackEnable().allocateStartSize( 1 * 1024*1024*1024).allocateIncrement(512 * 1024*1024).make();
    val dbmap = db.hashSet(setID).serializer(org.mapdb.Serializer.STRING).create()
      //db.hashSet("PairedEndSortedReadBuffer").keySerializer(org.mapdb.Serializer.STRING).valueSerializer(org.mapdb.Serializer.BYTE_ARRAY).create();
    val buffer = scala.collection.mutable.HashSet[String]();
    //var remCt : Int = 0;
    var comCt : Int = 0;
    var warnLevel : Int = maxReadCache;
    var maxSize : Int = 0;
    var elemCt : Int = 0;

    def +=( elem : String) : this.type = {
      if(buffer.size < maxReadCache){
        buffer.add(elem);
      } else {
        dbmap.add(elem)
        comCt = comCt + 1;
        if( comCt > commitCount ){
          db.commit();
          comCt = 0;
        }
      }
      this;
    }
    
    def iterator : Iterator[String] = {
      buffer.iterator ++ dbmap.asScala.iterator
      //.asScala.iterator.map{ k => (k,deserializeB(dbmap.get(k))) }
    }
    def -=( elem : String ) : this.type = {
      if( buffer.contains( elem ) ){
        buffer.remove(elem);
      } else if(dbmap.contains( elem )){
        dbmap.remove(elem);
        comCt = comCt + 1;
        if( comCt > commitCount ){
          db.commit();
          comCt = 0;
        }
      }
      this;
    }
    override def size : Int = {
      buffer.size + dbmap.getSize()
    }
    override def empty : DiskFallbackStringSet = {
      new DiskFallbackStringSet("emptySet");
    }
    override def contains(key: String) : Boolean = {
      buffer.contains( key ) || dbmap.contains( key )
    }
    //override def containsKey(key: String) : Boolean = {
    //  this.contains(key);
    //}
  }
  
  class DiskFallbackStringHashMap[B]( maxReadCache : Int = 1000000, commitCount : Int = 1000, warnMultiplier : Int = 10) extends scala.collection.mutable.Map[String,B] {
    val db = DBMaker.fileDB(new File("test.txt")).fileMmapEnableIfSupported().cleanerHackEnable().allocateStartSize( 1 * 1024*1024*1024).allocateIncrement(512 * 1024*1024).make();
    val dbmap = db.hashMap("PairedEndSortedReadBuffer").keySerializer(org.mapdb.Serializer.STRING).valueSerializer(org.mapdb.Serializer.BYTE_ARRAY).create();
    val buffer = scala.collection.mutable.HashMap[String,B]();
    //var remCt : Int = 0;
    var comCt : Int = 0;
    var warnLevel : Int = maxReadCache;
    var maxSize : Int = 0;
    var elemCt : Int = 0;
    
    def serializeB( s : B ) : Array[Byte] = {
      val baos : java.io.ByteArrayOutputStream = new java.io.ByteArrayOutputStream();
      val oos : java.io.ObjectOutputStream = new java.io.ObjectOutputStream(baos)
      oos.writeObject( s );
      oos.close();
      return baos.toByteArray();
    }
    def deserializeB( bb : Array[Byte] ) : B = {
      val ois : java.io.ObjectInputStream = new java.io.ObjectInputStream( new java.io.ByteArrayInputStream( bb ) );
      val s : B = ois.readObject().asInstanceOf[B];
      return s;
    }

    def +=( elem : (String,B)) : this.type = {
      if(buffer.size < maxReadCache){
        buffer.put( elem._1, elem._2 );
      } else {
        dbmap.put( elem._1, serializeB(elem._2) );
        comCt = comCt + 1;
        if( comCt > commitCount ){
          db.commit();
          comCt = 0;
        }
      }
      this;
    }
    
    def get( key : String ) : Option[B] = {
      if( buffer.contains( key ) ){
        return buffer.get(key)
      } else if(dbmap.containsKey( key )){
        return Some(deserializeB( dbmap.get(key)) )
      } else {
        return None;
      }
    }
    def iterator : Iterator[(String,B)] = {
      buffer.iterator ++ dbmap.getKeys().asScala.iterator.map{ k => (k,deserializeB(dbmap.get(k))) }
    }
    def -=( elem : String ) : this.type = {
      if( buffer.contains( elem ) ){
        buffer.remove(elem);
      } else if(dbmap.containsKey( elem )){
        dbmap.remove(elem);
        comCt = comCt + 1;
        if( comCt > commitCount ){
          db.commit();
          comCt = 0;
        }
      }
      this;
    }
    override def size : Int = {
      buffer.size + dbmap.sizeLong().toInt
    }
    override def empty : DiskFallbackStringHashMap[B] = {
      new DiskFallbackStringHashMap[B]();
    }
    override def contains(key: String) : Boolean = {
      buffer.contains( key ) || dbmap.containsKey( key )
    }
    override def remove(key : String) : Option[B] = {
      if( buffer.contains( key ) ){
        return buffer.remove(key);
      } else if(dbmap.containsKey( key )){
        val out = deserializeB( dbmap.get(key) );
        dbmap.remove(key);
        comCt = comCt + 1;
        if( comCt > commitCount ){
          db.commit();
          comCt = 0;
        }
        return Some(out);
      } else {
        return None;
      }
    }
    //override def containsKey(key: String) : Boolean = {
    //  this.contains(key);
    //}
  }
  
}