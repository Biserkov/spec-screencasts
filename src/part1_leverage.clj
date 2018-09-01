(ns part1-leverage)

;; user setup
(require '[clojure.spec.alpha :as s]
         '[clojure.spec.test.alpha :as test])

;; example fn
(defn my-index-of
  "Returns the index at which search appears in source"
  [source search]
  (clojure.string/index-of source search))

(my-index-of "foobar" "b")
(apply my-index-of ["foobar" "b"])

;; spec regex
(s/def ::index-or-args (s/cat :source string? :search string?))

;; validation
(s/valid? ::index-or-args ["foo" "f"])
(s/valid? ::index-or-args ["foo" 3])

;; conformance & destructuring
(s/conform ::index-or-args ["foo" "f"])
(s/unform ::index-or-args {:source "foo", :search "f"})

;; precise errors
(s/explain ::index-or-args ["foo" 3])
(s/explain-str ::index-or-args ["foo" 3])
(s/explain-data ::index-or-args ["foo" 3])

;; composition
(s/explain (s/every ::index-or-args) [["good" "a"]
                                      ["ok" "b"]
                                      ["bad" 42]])

;; example data generation
(s/exercise ::index-or-args)

;; assertion
(s/check-asserts true)
(s/assert ::index-or-args ["foo" "f"])
(s/assert ::index-or-args ["foo" [42]])

;; specing a function
(s/fdef my-index-of
        :args (s/cat :source string? :search string?)
        :ret nat-int?
        :fn #(<= (:ret %) (-> % :args :source count)))

;; documentation
(doc my-index-of)

;; generative testing
(->> (test/check `my-index-of) test/summarize-results)

;; instrumentation
(test/instrument `my-index-of)

(my-index-of "foo" 42)