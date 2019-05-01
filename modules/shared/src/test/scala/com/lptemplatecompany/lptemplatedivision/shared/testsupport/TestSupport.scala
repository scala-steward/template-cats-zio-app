package com.lptemplatecompany.lptemplatedivision.shared.testsupport

import cats.Eq
import cats.syntax.either._
import cats.syntax.eq._
import minitest.api.Asserts
import org.scalacheck.Gen
import scalaz.zio.{DefaultRuntime, IO, ZIO}

import scala.language.implicitConversions

final class TestSupportOps[A](val actual: A) extends Asserts {
  def shouldBe(expected: A): Boolean = {
    val result = expected == actual
    if (!result) {
      println(s"       => FAIL: expected[$expected]")
      println(s"                  actual[$actual]")
    }
    result
  }

  def shouldSatisfy(f: A => Boolean): Boolean = {
    val result = f(actual)
    if (!result) {
      println(s"       => FAIL:   doesn't satisfy, actual: [$actual]")
    }
    result
  }

  def assertIs(expected: A): Unit = {
    shouldBe(expected)
    assertEquals(actual, expected)
  }

  def assertSatisfies(f: A => Boolean): Unit = {
    assert(shouldSatisfy(f))
  }
}

trait ToTestSupportOps {
  implicit def `Ops for TestSupport`[A](actual: A): TestSupportOps[A] =
    new TestSupportOps[A](actual)
}

////

final class TestSupportEqOps[A: Eq](val actual: A) extends Asserts {
  def shouldBeEq(expected: A): Boolean = {
    val result = expected === actual
    if (!result) {
      println(s"       => FAIL: expected[$expected]")
      println(s"                  actual[$actual]")
    }
    result
  }

  def assertIsEq(expected: A): Unit = {
    shouldBeEq(expected)
    assert(actual === expected)
  }
}

trait ToTestSupportEqOps {
  implicit def `Ops for TestSupport Eq`[A: Eq](actual: A): TestSupportEqOps[A] =
    new TestSupportEqOps[A](actual)
}

////


trait TestSupportGens {
  def genBoolean: Gen[Boolean] =
    Gen.posNum[Int].map(_ % 2 == 0)

  def genNonEmptyString(n: Int): Gen[String] =
    for {
      count <- Gen.choose(1, n)
      chars <- Gen.listOfN(count, Gen.alphaChar)
    } yield chars.mkString

  def multilineGen(genTestString: Gen[String]): Gen[(List[String], String)] =
    for {
      scount <- Gen.chooseNum[Int](0, 20)
      strings <- Gen.listOfN(scount, genTestString)
    } yield strings -> strings.flatMap(s => List("\n", s, "\n")).mkString("\n")
}

////

final class IOSyntaxSafeOpsTaskTesting[E, A](t: IO[E, A]) extends DefaultRuntime {
  def runSync(): Either[List[E], A] =
    unsafeRunSync(t)
      .fold(
        _.fold(List[E]()) {
          case (errors, cause) => errors ++ cause.failures
        }.asLeft,
        _.asRight
      )

}

trait ToIOSyntaxSafeOpsTaskTesting {
  implicit def implToIOSyntaxSafeOpsTaskTesting[E, A](t: IO[E, A]): IOSyntaxSafeOpsTaskTesting[E, A] =
    new IOSyntaxSafeOpsTaskTesting[E, A](t)
}

////

trait TestSupport
  extends ToTestSupportOps
    with ToTestSupportEqOps
    with TestSupportGens
    with ToIOSyntaxSafeOpsTaskTesting

object testsupportinstances
  extends TestSupport
