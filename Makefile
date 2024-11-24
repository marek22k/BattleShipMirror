.PHONY: fatJar proguardedJar check docs clean

fatJar:
	./gradlew fatJar

proguardedJar:
	./gradlew proguardedJar

check:
	./gradlew check

docs:
	./gradlew javadoc

clean:
	./gradlew clean
