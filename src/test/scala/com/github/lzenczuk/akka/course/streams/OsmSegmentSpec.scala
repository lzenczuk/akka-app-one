package com.github.lzenczuk.akka.course.streams

import com.github.lzenczuk.akka.course.streams.OsmSegment.{NodeElementString, RelationElementString, WayElementString}
import org.scalatest._

/**
  * Created by dev on 05/09/16.
  */
class OsmSegmentSpec extends FlatSpec with Matchers{

  "Idle OsmSegment" should "parse single line full osm node segment" in {
    val idle: OsmSegment = Idle()

    val line: String = "\t<node id=\"12867432\" lat=\"53.3794931\" lon=\"-6.3176693\" version=\"2\" timestamp=\"2014-04-13T11:15:24Z\" changeset=\"21662684\" uid=\"114051\" user=\"Dafo43\"/>"
    val nextSegment: OsmSegment = idle.parse(line)

    nextSegment should be (FullSegment(line))
  }

  "Idle OsmSegment" should "parse multiple lines of full osm node segment" in {
    val idle: OsmSegment = Idle()

    val line1: String = "\t<node id=\"12867432\" lat=\"53.3794931\" lon=\"-6.3176693\" version=\"2\" timestamp=\"2014-04-13T11:15:24Z\" changeset=\"21662684\" uid=\"114051\" user=\"Dafo43\"/>"
    val line2: String = "\t<node id=\"12867433\" lat=\"53.3794931\" lon=\"-6.3176693\" version=\"2\" timestamp=\"2014-04-13T11:15:24Z\" changeset=\"21662684\" uid=\"114051\" user=\"Dafo43\"/>"
    val line3: String = "\t<node id=\"12867434\" lat=\"53.3794931\" lon=\"-6.3176693\" version=\"2\" timestamp=\"2014-04-13T11:15:24Z\" changeset=\"21662684\" uid=\"114051\" user=\"Dafo43\"/>"
    val nextSegment1: OsmSegment = idle.parse(line1)
    val nextSegment2: OsmSegment = nextSegment1.parse(line2)
    val nextSegment3: OsmSegment = nextSegment2.parse(line3)

    nextSegment1 should be (FullSegment(line1))
    nextSegment2 should be (FullSegment(line2))
    nextSegment3 should be (FullSegment(line3))
  }

  "Idle OsmSegments" should "parse multiple lines of osm node segment" in {
    val idle: OsmSegment = Idle()

    val line1: String = "\t<node id=\"12867390\" lat=\"53.3642378\" lon=\"-6.2991798\" version=\"3\" timestamp=\"2013-07-01T19:33:53Z\" changeset=\"16782242\" uid=\"6367\" user=\"mackerski\">"
    val line2: String = "\t\t<tag k=\"highway\" v=\"mini_roundabout\"/>"
    val line3: String = "\t\t<tag k=\"direction\" v=\"clockwise\"/>"
    val line4: String = "\t</node>"
    val line5: String = "\t<node id=\"12867434\" lat=\"53.3794931\" lon=\"-6.3176693\" version=\"2\" timestamp=\"2014-04-13T11:15:24Z\" changeset=\"21662684\" uid=\"114051\" user=\"Dafo43\"/>"

    val nextSegment1: OsmSegment = idle.parse(line1)
    val nextSegment2: OsmSegment = nextSegment1.parse(line2)
    val nextSegment3: OsmSegment = nextSegment2.parse(line3)
    val nextSegment4: OsmSegment = nextSegment3.parse(line4)
    val nextSegment5: OsmSegment = nextSegment4.parse(line5)

    nextSegment1 should be (OpenSegment(NodeElementString, line1))
    nextSegment2 should be (OpenSegment(NodeElementString, List(line1,line2).mkString("\n")))
    nextSegment3 should be (OpenSegment(NodeElementString, List(line1,line2,line3).mkString("\n")))
    nextSegment4 should be (FullSegment(List(line1,line2,line3,line4).mkString("\n")))
    nextSegment5 should be (FullSegment(line5))
  }

  "Idle OsmSegments" should "parse multiple lines of osm way segment" in {
    val idle: OsmSegment = Idle()

    val line1: String = "\t<way id=\"438331199\" version=\"1\" timestamp=\"2016-08-21T21:15:57Z\" changeset=\"41602097\" uid=\"19612\" user=\"brianh\">"
    val line2: String = "\t\t<nd ref=\"4360639412\"/>"
    val line3: String = "\t\t<tag k=\"addr:housenumber\" v=\"16\"/>"
    val line4: String = "\t</way>"
    val line5: String = "\t<node id=\"12867434\" lat=\"53.3794931\" lon=\"-6.3176693\" version=\"2\" timestamp=\"2014-04-13T11:15:24Z\" changeset=\"21662684\" uid=\"114051\" user=\"Dafo43\"/>"

    val nextSegment1: OsmSegment = idle.parse(line1)
    val nextSegment2: OsmSegment = nextSegment1.parse(line2)
    val nextSegment3: OsmSegment = nextSegment2.parse(line3)
    val nextSegment4: OsmSegment = nextSegment3.parse(line4)
    val nextSegment5: OsmSegment = nextSegment4.parse(line5)

    nextSegment1 should be (OpenSegment(WayElementString, line1))
    nextSegment2 should be (OpenSegment(WayElementString, List(line1,line2).mkString("\n")))
    nextSegment3 should be (OpenSegment(WayElementString, List(line1,line2,line3).mkString("\n")))
    nextSegment4 should be (FullSegment(List(line1,line2,line3,line4).mkString("\n")))
    nextSegment5 should be (FullSegment(line5))
  }

  "Idle OsmSegments" should "parse single line node and multiple lines of osm way segment" in {
    val idle: OsmSegment = Idle()

    val line1: String = "\t<node id=\"12867434\" lat=\"53.3794931\" lon=\"-6.3176693\" version=\"2\" timestamp=\"2014-04-13T11:15:24Z\" changeset=\"21662684\" uid=\"114051\" user=\"Dafo43\"/>"
    val line2: String = "\t<way id=\"438331199\" version=\"1\" timestamp=\"2016-08-21T21:15:57Z\" changeset=\"41602097\" uid=\"19612\" user=\"brianh\">"
    val line3: String = "\t\t<nd ref=\"4360639412\"/>"
    val line4: String = "\t\t<tag k=\"addr:housenumber\" v=\"16\"/>"
    val line5: String = "\t</way>"

    val nextSegment1: OsmSegment = idle.parse(line1)
    val nextSegment2: OsmSegment = nextSegment1.parse(line2)
    val nextSegment3: OsmSegment = nextSegment2.parse(line3)
    val nextSegment4: OsmSegment = nextSegment3.parse(line4)
    val nextSegment5: OsmSegment = nextSegment4.parse(line5)

    nextSegment1 should be (FullSegment(line1))
    nextSegment2 should be (OpenSegment(WayElementString, line2))
    nextSegment3 should be (OpenSegment(WayElementString, List(line2,line3).mkString("\n")))
    nextSegment4 should be (OpenSegment(WayElementString, List(line2,line3,line4).mkString("\n")))
    nextSegment5 should be (FullSegment(List(line2,line3,line4,line5).mkString("\n")))
  }

  "Idle OsmSegments" should "parse multiple lines of osm relation segment" in {
    val idle: OsmSegment = Idle()

    val line1: String = "\t<relation id=\"6359197\" version=\"1\" timestamp=\"2016-06-26T14:39:31Z\" changeset=\"40302077\" uid=\"19612\" user=\"brianh\">"
    val line2: String = "\t\t<member type=\"way\" ref=\"94202499\" role=\"outer\"/>"
    val line3: String = "\t\t<tag k=\"type\" v=\"multipolygon\"/>"
    val line4: String = "\t</relation>"
    val line5: String = "\t<node id=\"12867434\" lat=\"53.3794931\" lon=\"-6.3176693\" version=\"2\" timestamp=\"2014-04-13T11:15:24Z\" changeset=\"21662684\" uid=\"114051\" user=\"Dafo43\"/>"

    val nextSegment1: OsmSegment = idle.parse(line1)
    val nextSegment2: OsmSegment = nextSegment1.parse(line2)
    val nextSegment3: OsmSegment = nextSegment2.parse(line3)
    val nextSegment4: OsmSegment = nextSegment3.parse(line4)
    val nextSegment5: OsmSegment = nextSegment4.parse(line5)

    nextSegment1 should be (OpenSegment(RelationElementString, line1))
    nextSegment2 should be (OpenSegment(RelationElementString, List(line1,line2).mkString("\n")))
    nextSegment3 should be (OpenSegment(RelationElementString, List(line1,line2,line3).mkString("\n")))
    nextSegment4 should be (FullSegment(List(line1,line2,line3,line4).mkString("\n")))
    nextSegment5 should be (FullSegment(line5))
  }
}
