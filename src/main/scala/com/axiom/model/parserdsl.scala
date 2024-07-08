package com.axiom.model
import fastparse._
import SingleLineWhitespace._  //for skipping whiteespaces and tabs


object parserdsl:
  import queryparser._
  import FilterTerm._

  private def stringOption(s:String):Option[String] =
    s match {
      case "" => None
      case x  => Some(x)
    }
 
// //fullname
  private def text[$: P] = CharsWhileIn("a-zA-Z")
  private def optionLastName [$:P] = P(text).!.map(stringOption)
  private def lastName[$: P] = P(optionLastName).map{x =>
    FULLNAME(x,None)
  }
  private def optionfirstname [$: P]  = P(namecomma~text).!.map { x =>
    stringOption(x.replace(",",""))
  }
  private def digit[$: P] = CharIn("0-9")
  private def floor[$: P] = P((digit ~ &(" ")) | digit).!
  def floorwing[$:P] = P(floor~wing.?).map {(flr,wing) => FLOORWING(flr,wing.map(_.toUpperCase))}

  private def wing[$: P]  = CharIn("a-hA-H").!

  private def digit3[$:P] = CharIn("0-9").rep(min=3,max=3)
  private def room[$:P]   = P(digit3 ~ &(" ") | digit3).!.map {x =>   ROOMBED(x,None) }

  private def firstname[$:P] = P(optionfirstname).map{  x =>
    FULLNAME(None,x)
  }
  private def namecomma[$: P] = P(",")
  private def mrp[$:P] = P("@"~CharsWhileIn("a-zA-Z")).rep(max=5).!.map{x => MRP(stringOption(x.drop(1).toUpperCase()))}

  private def fullname[$: P] = P(optionLastName ~ optionfirstname).map{(last,first) => 
      FULLNAME(last,first)
  }

  private def namesearch[$:P] = P(firstname|fullname | lastName) 
  
  def search[$: P] :ParsingRun[FilterTerms]  = P(room.? ~floorwing.? ~mrp.?  ~namesearch.? ).map((rm,fw,mrp,fn) => 
    FilterTerms(fw.asInstanceOf[Option[FLOORWING]],
      mrp.asInstanceOf[Option[MRP]], 
      rm.asInstanceOf[Option[ROOMBED]],
      fn.asInstanceOf[Option[FULLNAME]]))
  