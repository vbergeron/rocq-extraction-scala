/* Extracted from header_comment.v. */

package com.foo

import language.experimental.pureFunctions

object Main {

sealed trait Nat
final case class O() extends Nat
final case class S(_1: Nat) extends Nat


def twice(f: Nat -> Nat)(n: Nat): Nat =
  f(f(n))

}

