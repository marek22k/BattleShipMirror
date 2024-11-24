.PHONY: jar check docs clean

fatJar:
	./gradlew fatJar

check:
	./gradlew check

docs:
	./gradlew javadoc

clean:
	./gradlew clean
