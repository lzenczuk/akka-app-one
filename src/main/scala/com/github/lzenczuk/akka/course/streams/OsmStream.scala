package com.github.lzenczuk.akka.course.streams

import java.io.{FileInputStream, InputStream}
import java.nio.charset.Charset
import java.nio.file.Paths

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, RunnableGraph, Sink, Source, StreamConverters}
import akka.util.ByteString
import org.apache.commons.compress.compressors.bzip2.{BZip2CompressorInputStream, BZip2CompressorOutputStream}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

/**
  * Created by dev on 02/09/16.
  */

/*
object OsmSegment {
  sealed trait OsmSegmentStatus
  case object Init extends OsmSegmentStatus
  case object FullSegment extends OsmSegmentStatus
  case object OpenSegment extends OsmSegmentStatus
}

case class OsmSegment(status:OsmSegmentStatus, content:String)
*/



object OsmStream extends App {
  implicit private val system: ActorSystem = ActorSystem("osm-reading-system")
  implicit private val materializer: ActorMaterializer = ActorMaterializer()

  /*private val osmFile: Source[ByteString, Future[IOResult]] = FileIO.fromPath(Paths.get("/home/dev/Documents/osm/dublin/dublin_ireland.osm.bz2"))
  private val javaInputStream: Sink[ByteString, InputStream] = StreamConverters.asInputStream()

  osmFile.toMat(javaInputStream)(Keep.both).run()*/

  private val uncompressedOsmFileSource: Source[ByteString, Future[IOResult]] =
    StreamConverters.fromInputStream(() => new FileInputStream("/home/dev/Documents/osm/dublin/dublin_ireland.osm.bz2"))
    .map(bs => {
      println(s"------------> File read: ${bs.length}")
      bs
    })

  private val bytesToStringLines: Flow[ByteString, String, NotUsed] = Framing.delimiter(ByteString("\n"), 1024).map(bs => {
    println(s"---------------> frame: ${bs.length}")
    bs.decodeString(Charset.defaultCharset())
  })

  /*
  val groupNodes: Flow[String, String, NotUsed] = Flow[String].scan((0, ""))((tup, line) => tup match {
    case ((0, "") | (3, _)) if line.startsWith("\t<node") && line.endsWith("/>") => (3, line)
    case ((0, "") | (3, _)) if line.startsWith("\t<node") => (1, line)
    case (0, "") | (3, _) => (0, "")
    case (1, s) if line.contains("</node>") => (3, s+"\n"+line)
    case (1, s) => (1, s+"\n"+line)
  }).filter((tup) => tup._1==3)
    .map(_._2)
   */

  val groupNodes: Flow[String, String, NotUsed] = Flow[String]
    .scan(Idle().asInstanceOf[OsmSegment])((segment:OsmSegment, line:String) => segment.parse(line))
    .filter(s => s.isInstanceOf[FullSegment]).map(s => s.content)

  private val osmStream: RunnableGraph[Future[IOResult]] =
    uncompressedOsmFileSource
      .via(bytesToStringLines)
      .via(groupNodes)
      .to(Sink.foreach(line => {}))

  osmStream.run().andThen{
    case Success(ioResult) =>
      println(s"Success read: ${ioResult.count/1048576} MB")
    case Failure(throwable) => println(s"Error: ${throwable.getMessage}")
  }.andThen{
    case _ => system.terminate()
  }

  /*private val result: Sink[Any, Future[Done]] = Sink.ignore

  osmFile.to(result).run().andThen{
    case Success(ioResult) => println(s"Success read: ${ioResult.count}")
    case Failure(throwable) => println(s"Error: ${throwable.getMessage}")
  }.andThen{
    case _ =>
      println("Terminating actor system")
      system.terminate()
  }*/
}
