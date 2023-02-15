(defproject hexenhammer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [hiccup "1.0.5"]
                 [garden "1.3.10"]
                 [ring "1.9.6"]
                 [compojure "1.7.0"]]
  :profiles {:dev {:dependencies [[midje/midje "1.10.9"]]}}
  :repl-options {:init-ns hexenhammer.core})
