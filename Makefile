# IDDD_Samples — `make` lists the targets.
SHELL := /bin/bash
.DEFAULT_GOAL := help

export JAVA_HOME := $(shell /usr/libexec/java_home -v 25)
GRADLE := ./gradlew

.PHONY: help up down test build clean

## help: list targets
help:
	@grep -E '^## ' $(MAKEFILE_LIST) | sed -E 's/^## ([a-z]+): /  \1\t/' | column -t -s $$'\t'

## up: start MySQL + RabbitMQ and block until both are healthy (MySQL init scripts run on first start only)
up:
	docker compose up -d --wait

## down: stop and remove the containers (no volumes, so the next `up` re-runs the init scripts)
down:
	docker compose down

## test: run every module's tests against the running containers (Java 25 via java_home)
test:
	$(GRADLE) test --continue

## build: full build incl. tests
build:
	$(GRADLE) build

## clean: remove all Gradle build outputs
clean:
	$(GRADLE) clean
