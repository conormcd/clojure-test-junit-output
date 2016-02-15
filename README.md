# clojure-test-junit-output

Make it easier to output your test results in JUnit format. Yes, there's
`clojure.test.junit` but it doesn't do timing and it requires you to choose
between the normal `clojure.test` output (read by humans) and JUnit XML which
is not read by most people if they have anything to say about it.

## Usage

Add this library to your dependencies. You can put it in your `:test` profile
if you'd like.

Then all you need to do is wrap your tests. You can doe this with a `:once`
fixture like so:

```clojure
; Add this to your :require vector in your ns declaration.
[clojure-test-junit-output.core :refer (with-junit-output)]

(clojure.test/use-fixtures :once (with-junit-output "/where/you/want.xml"))
```

## License

This library is distributed under a two clause BSD-style license. See the
`LICENSE` file for details.
