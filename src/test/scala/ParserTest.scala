package org.aurora.ParserTest

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest._

import wordspec._
import matchers._


import fastparse._,  SingleLineWhitespace._  //for skipping whiteespaces and tabs


  enum ParserState {
    case A extends ParserState
    case B extends ParserState
  }



class ParserTest extends AnyWordSpec with should.Matchers{
"successful parse" should {
  "return Parsed.Success" in {
    
    def a[$:P] = CharIn("a").! map { a =>  ParserState.A }
    val r @  Parsed.Success(value,index) = parse("a",a(_)) : @unchecked

    value should be (ParserState.A)
  }
}

"successful parse combinator" should {
  "return (A,B)" in {
    def a[$:P] = CharIn("a").! map { a =>  ParserState.A }
    def b[$:P] = CharIn("b").! map { a =>  ParserState.B }

    def combinator[$:P] = P(a~b)   

    val r  @Parsed.Success(value,index)= parse("ab",combinator(_)) : @unchecked
    value should be((ParserState.A,ParserState.B))
  }
} 


"look ahead to guarantee text ends in SPACE" should {
  "work" in {
    def a[$:P] = P(("a" ~ &(" "))|"a").! map { a =>  ParserState.A } 

    ( parse("a", a(_)) : @unchecked ) match {
      case Parsed.Success(value,index) => ()
      case Parsed.Failure(label,index,extra) => fail()
    }

    ( parse("a ", a(_)) : @unchecked ) match {
      case Parsed.Success(value,index) => ()
      case Parsed.Failure(label,index,extra) => fail()
    }
 
  }

}

"a parse failure" should {
  "return Parsed.Failure" in {
    def a[$:P] =  CharIn("a").! map { a =>  ParserState.A }
    
    val r = parse("b",a(_))

    r match {
      case f @ Parsed.Failure(label,index,extra) => {
        label should be("")
        index should be(0)
        info(s"${extra.stack}")
      }
      case s @ Parsed.Success(value,index) => fail()
    }
  }

  "skipping a character (such as the hyphen prefix in \"-ab\")" should {
    "parse to \"ab\"" in {
      def hyphen[$:P] = P("-")
      def a[$:P] = P(hyphen~"a").!.map(_.substring(1)) //skipping the hyphen here.
      def b[$:P] = P("b").!

      def hyphenAB[$:P] = P(a~b).map((a,b) => a+b)

      val r = parse("-ab",hyphenAB(_))

      r match {
        case s @ Parsed.Success(value,index) => value should be("ab")
        case f @ Parsed.Failure(label,index,extra) => fail()
      }

    }
  }
}}
