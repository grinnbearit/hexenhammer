(defproject hexenhammer "0.2.0"
  :description "A Server/Client implementation of Hexenhammer"
  :url "https://docs.google.com/document/d/1LclOjQDjc4aSOkM2nEjhJy2rzv2qRhul3Mm_cfHb4m4/edit?usp=sharing"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [hiccup "2.0.0-RC1"]
                 [garden "1.3.10"]
                 [ring "1.10.0"]
                 [compojure "1.7.0"]]
  :profiles {:dev {:dependencies [[midje/midje "1.10.9"]]}}
  :repl-options {:init-ns hexenhammer.core})
