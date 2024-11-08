package com.axiom.model

import com.axiom.model.PatientLocation

object queryparser:

  enum FilterTerm: 
    case FLOORWING(floor:String,wing:Option[String])
    case ROOMBED(room:String,bed:Option[String]) extends FilterTerm
    case MRP(text:Option[String]) extends FilterTerm
    case FULLNAME(last:Option[String],first:Option[String])

  def parseFilterTerms(s:String):FilterTerms = 
    import fastparse._
    import SingleLineWhitespace._  //for skipping whiteespaces and tabs
    import parserdsl.search
    val r @  Parsed.Success(value,index) = parse(s,search(using _)) : @unchecked
    value



  import FilterTerm._ 
  case class FilterTerms( fw: Option[FLOORWING], mrp:Option[MRP],rm: Option[ROOMBED], fn: Option[FULLNAME])

  //TODO IMPLEMENT THIS WITHIN DATAIMPORTCSVS
  trait IncludeMethods[T] (var st:FilterTerms) :
    def includeFullName(patientdata:T):Boolean
    def includeFloorWing(patientdata:T):Boolean
    def includeMrp(patientdata:T):Boolean
    def includeRoom(patientdata:T):Boolean
    def include(patientdata:T):Boolean =
      includeFloorWing(patientdata) && includeMrp(patientdata) && includeFullName(patientdata) && includeRoom(patientdata)
      
  import FilterTerm._
  given IncludeMethods[PatientLocation](st=null) with
    def includeFullName(patientLocation:PatientLocation):Boolean = st.fn match {
      case None => true
      case Some(FULLNAME(last,first)) =>  
        val result = (last,first) match {
          case (None,None) => true
          case (Some(x),None) => patientLocation.lastname.toUpperCase.startsWith(x.toUpperCase())
          case (None,Some(y)) => patientLocation.firstname.toUpperCase.startsWith(y.toUpperCase())
          case (Some(x),Some(y)) => patientLocation.lastname.toUpperCase.startsWith(x.toUpperCase()) && patientLocation.firstname.toUpperCase.startsWith(y.toUpperCase())
        }
        result
    }
    def includeRoom(patientLocation:PatientLocation):Boolean = st.rm match {
      case None => true
      case Some(ROOMBED(room,None)) => patientLocation.room.toUpperCase.startsWith(room.toUpperCase())
      case Some(ROOMBED(room,Some(bed))) => true //TODO IMPLEMENT BED
    }
    def includeFloorWing(patientLocation:PatientLocation):Boolean = 
      import fastparse._
      import SingleLineWhitespace._  //for skipping whiteespaces and tabs
      import parserdsl.floorwing

      st.fw match {
        case None => true
        case Some(FLOORWING(floor,None)) => 
          val r @  Parsed.Success(value:FLOORWING,index) = parse(patientLocation.floorwing,floorwing(using _)) : @unchecked
          FLOORWING(value.floor, None)==st.fw.get
        case Some(FLOORWING(floor,Some(wing))) => 
          val r @  Parsed.Success(value:FLOORWING,index) = parse(patientLocation.floorwing,floorwing(using _)) : @unchecked
          FLOORWING(value.floor, value.wing)==st.fw.get
      }
    def includeMrp(patientLocation:PatientLocation):Boolean =  st.mrp match {
        case None => true
        case Some(MRP(text)) => {
          val result = text match {
            case None => true
            case Some(x) => patientLocation.mrp.contains(x)
          }
          result
        }
      }
      
   
      
end queryparser