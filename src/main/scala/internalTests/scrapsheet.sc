package main.scala.internalTests



object scrapsheet {
  println("Welcome to the Scala worksheet")
  

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;
import java.io.File;
//import scala.collection.JavaConversions._
import java.io._;
import internalUtils.commandLineUI._;
import internalUtils.Reporter._;

//import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import internalUtils.optionHolder._;
import internalUtils.Reporter._;
import internalUtils.stdUtils._;
import internalUtils.genomicAnnoUtils._;
import internalUtils.GtfTool._;
import internalUtils.commandLineUI._;
import internalUtils.fileUtils._;
import internalUtils.TXUtil._;
import internalUtils.TXUtil

import internalUtils.VcfTool._;

import internalUtils.VcfTool;

import htsjdk.variant._;
import htsjdk.variant.variantcontext._;
import htsjdk.variant.vcf._;

import internalUtils.genomicUtils._;
import internalUtils.commonSeqUtils._;
 
   println("Welcome to the Scala worksheet")
 
     val parser : SFilterLogicParser[SVcfVariantLine] = internalUtils.VcfTool.sVcfFilterLogicParser;
     
    val filter : SFilterLogic[SVcfVariantLine] = parser.parseString("TRUE() AND ( ALT.eq(A) )");
   
 
    parser.parseString("TRUE() AND ( (ALT.eq(A)) OR (NOT INFO.inAnyOf(A,10,20,30) ) ) ").printTree()
 
 
 
   
}