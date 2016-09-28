;; setup
(require '[clojure.spec :as s]
         '[clojure.spec.test :as test])

;; fn under test
(defn my-index-of
  "Returns the index at which search appears in source"
  [source search & opts]
  (apply clojure.string/index-of source search opts))

;; fspec
(s/fdef my-index-of
        :args (s/cat :source string? :search string?)
        :ret nat-int?
        :fn #(<= (:ret %) (-> % :args :source count)))

(s/exercise-fn `my-index-of)

;; alt
(s/conform (s/alt :string string? :char char?)
           ["foo"])

(s/conform (s/alt :string string? :char char?)
           [\f])

(s/explain (s/alt :string string? :char char?)
           [42])

;; alt in args
(s/fdef my-index-of
        :args (s/cat :source string?
                     :search (s/alt :string string?
                                    :char char?))
        :ret nat-int?
        :fn #(<= (:ret %) (-> % :args :source count)))

(s/exercise-fn `my-index-of)

;; quantification
(s/conform (s/? nat-int?) [])
(s/conform (s/? nat-int?) [1])
(s/explain (s/? nat-int?) [:a])
(s/explain (s/? nat-int?) [1 2])

;; optional argument
(s/fdef my-index-of
        :args (s/cat :source string?
                     :search (s/alt :string string?
                                    :char char?)
                     :from (s/? nat-int?))
        :ret nat-int?
        :fn #(<= (:ret %) (-> % :args :source count)))

(s/exercise-fn `my-index-of)

;; example testing
(assert (= 8 (my-index-of "testing with spec" "w")))

;; test/check generative testing
(->> (test/check `my-index-of) test/summarize-results)

;; nilable
(s/conform (s/nilable string?) "foo")
(s/conform (s/nilable string?) nil)
(s/conform (s/nilable string?) 42)

;; nilable :ret
(s/fdef my-index-of
        :args (s/cat :source string?
                     :search (s/alt :string string?
                                    :char char?)
                     :from (s/? nat-int?))
        :ret (s/nilable nat-int?)
        :fn #(<= (:ret %) (-> % :args :source count)))

(->> (test/check `my-index-of) test/summarize-results)

;; or
(s/fdef my-index-of
        :args (s/cat :source string?
                     :search (s/alt :string string?
                                    :char char?)
                     :from (s/? nat-int?))
        :ret (s/nilable nat-int?)
        :fn (s/or
              :not-found #(nil? (:ret %))
              :found #(<= (:ret %) (-> % :args :source count))))

(->> (test/check `my-index-of) test/summarize-results)

;; calling a speced fn
(defn which-came-first
  "Returns :chicken of :egg, depending on which string appears
  first in s, starting from position from."
  [s from]
  (let [c-idx (my-index-of s "chicken" :from from)
        e-idx (my-index-of s "egg" :from from)]
    (cond
      (< c-idx e-idx) :chicken
      (< e-idx c-idx) :egg)))

;; Stacktrace Assisted Debugging
(which-came-first "the chicken or the egg" 0)
(clojure.repl/pst)

;; instrumentation
(test/instrument `my-index-of)
(which-came-first "the chicken or the egg" 0)

;; test + instrumentation
(s/fdef which-came-first
        :args (s/cat :source string? :from nat-int?)
        :ret #{:chicken :egg})

(->> (test/check `which-came-first) test/summarize-results)