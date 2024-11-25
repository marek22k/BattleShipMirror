MAKEFLAGS := -j1

.PHONY: all fatJar proguardedJar check docs clean

all: check fatJar proguardedJar

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
