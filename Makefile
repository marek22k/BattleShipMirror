.PHONY: jar check docs clean

jar:
	./gradlew fatJar

check:
	./gradlew check

docs:
	./gradlew javadoc

clean:
	./gradlew clean
