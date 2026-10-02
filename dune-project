(lang dune 3.21)
(using rocq 0.11)

(name rocq-extraction-scala)

(generate_opam_files true)

(license LGPL-2.1-only)

(maintainers "Valentin Bergeron <bergeron.valentin@gmail.com>")

(authors "Valentin Bergeron")

(source
 (github vbergeron/rocq-extraction-scala))

(package
 (name rocq-extraction-scala)
 (synopsis "Scala 3 extraction backend for the Rocq Prover")
 (description
  "A Rocq plugin adding Scala as a target language of extraction: once loaded, `Extraction Language Scala` makes the extraction commands produce Scala 3 source.")
 (depends
  (rocq-runtime (>= 9.4~))
  (rocq-core (>= 9.4~))))
