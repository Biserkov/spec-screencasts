(ns part3-customizing-generators)

;; setup
(require '[clojure.spec.alpha :as s]
         '[clojure.spec.gen.alpha :as gen]
         '[clojure.spec.test.alpha :as test]
         '[clojure.string :as str])

;; arbitrary predicates are not efficient constructors

(s/def ::id (s/and string?
                   #(str/starts-with? % "FOO-")))

;; (s/exercise ::id)
;; ExceptionInfo Couldn't satisfy such-that predicate after 100 tries.

;; transform an existing generator
(defn foo-gen
  []
  (->> (s/gen (s/int-in 1 100))
       (gen/fmap #(str "FOO-" %))))
(s/exercise ::id 10 {::id foo-gen})

;; add generator to spec registry
(s/def ::id (s/spec
              (s/and string?
                   #(str/starts-with? % "FOO-"))
              :gen foo-gen))
(s/exercise ::id)

;; lookup
(s/def ::lookup (s/map-of keyword? string? :min-count 1))
(s/exercise ::lookup)

;; dependent-values
(s/def ::lookup-finding-k (s/and (s/cat :lookup ::lookup
                                        :k keyword?)
                                 (fn [{:keys [lookup k]}]
                                   (contains? lookup k))))
;; (s/exercise ::lookup-finding-k)
;; ExceptionInfo Couldn't satisfy such-that predicate after 100 tries.

;; generate and bind a model
(defn lookup-finding-k-gen
  []
  (gen/bind
    (s/gen ::lookup)
    #(gen/tuple
       (gen/return %)
       (gen/elements (keys %)))))


(s/exercise ::lookup-finding-k 10
            {::lookup-finding-k lookup-finding-k-gen})

;; previous example
(defn my-index-of
  "Returns the index at which search appears in source"
  [source search]
  (str/index-of source search))

(s/fdef my-index-of
        :args (s/cat :source string?
                     :search string?))

;; rarely generates a findable, nontrivial string
(s/exercise-fn `my-index-of)

;; constructively generate a string and substring
(def model (s/cat :prefix string?
                  :match string?
                  :suffix string?))
(defn gen-string-and-substring
  []
  (gen/fmap
    (fn [[prefix match suffix]] [(str prefix match suffix) match])
    (s/gen model)))

;; always generate a findable string
(s/def ::my-index-of-args (s/cat :source string?
                                 :search string?))
(s/fdef my-index-of
        :args (s/spec ::my-index-of-args
                      :gen gen-string-and-substring))
(s/exercise-fn `my-index-of)

;; combining models with one-of
(defn gen-my-index-of-args
  []
  (gen/one-of [(gen-string-and-substring)
               (s/gen ::my-index-of-args)]))

(s/fdef my-index-of
        :args (s/spec ::my-index-of-args
                      :gen gen-my-index-of-args))
(s/exercise-fn `my-index-of)