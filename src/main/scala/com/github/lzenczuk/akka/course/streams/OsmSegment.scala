package com.github.lzenczuk.akka.course.streams

import com.github.lzenczuk.akka.course.streams.OsmSegment.{ElementString, NodeElementString, RelationElementString, WayElementString}

/**
  * Created by dev on 05/09/16.
  */

object OsmSegment {
  trait ElementString {
    def elementString:String
  }
  object NodeElementString extends ElementString {
    def elementString = "node"
  }

  object WayElementString extends ElementString {
    def elementString = "way"
  }

  object RelationElementString extends ElementString {
    def elementString = "relation"
  }

}

trait OsmSegment {
  def content:String
  def parse(line: String): OsmSegment = {
    line match {
      case l if l.startsWith(s"\t<${NodeElementString.elementString}") && l.endsWith("/>") => FullSegment(line)
      case l if l.startsWith(s"\t<${NodeElementString.elementString}") => OpenSegment(NodeElementString, line)
      case l if l.startsWith(s"\t<${WayElementString.elementString}") && l.endsWith("/>") => FullSegment(line)
      case l if l.startsWith(s"\t<${WayElementString.elementString}") => OpenSegment(WayElementString, line)
      case l if l.startsWith(s"\t<${RelationElementString.elementString}") && l.endsWith("/>") => FullSegment(line)
      case l if l.startsWith(s"\t<${RelationElementString.elementString}") => OpenSegment(RelationElementString, line)
      case _ => Idle()
    }
  }
}

case class Idle() extends OsmSegment {
  def content = ""
}

case class FullSegment(content:String) extends OsmSegment{}

case class OpenSegment(es:ElementString, content:String) extends OsmSegment {
  override def parse(line: String): OsmSegment = {

    line match {
      case l if line.contains(s"</${es.elementString}>") => FullSegment(content + "\n" +line)
      case _ => OpenSegment(es, content + "\n" + line)
    }
  }
}

