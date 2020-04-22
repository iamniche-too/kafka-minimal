# Memory problem with Kafka Client (python & Java)

A problem has been identified with both Kafka Clients in terms of increasing memory use for long-running processes.

This problem was first noticed on long-running processes on GCP (Google Cloud Platform) since pods were being evicted due to memory use.

This repo documents some simple tests that highlight the problem.

The repo has the follownig structure:

README.md - this document
java/src/ - Java source for Kafka consumer 
java/test/ - java source for minimal test
python/src/  - Python source for Kafka consumer 
python/test/ - Pythn source for minimal test
docker/ - Dockerfiles
run_java_test.sh - script to rin the Java test
run_python_test.sh - script to run the Python test
