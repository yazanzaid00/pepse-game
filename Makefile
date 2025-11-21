# Simple Makefile for the Pepse game
# Usage:
#   make        # builds and runs the game
#   make run    # builds (if needed) and runs
#   make build  # compile only
#   make clean  # remove compiled classes

JAVAC   := javac
JAVA    := java
BIN_DIR := bin
JAR     := lib/DanoGameLab.jar
MAIN    := pepse.PepseGameManager
SOURCES := $(shell find pepse -name '*.java')

.PHONY: default build run clean

default: run

$(BIN_DIR): $(SOURCES)
	@rm -rf $(BIN_DIR)
	@mkdir -p $(BIN_DIR)
	@echo "Compiling sources..."
	@$(JAVAC) -cp "$(JAR)" -d $(BIN_DIR) $(SOURCES)

build: $(BIN_DIR)

run: build
	@echo "Running $(MAIN)..."
	@$(JAVA) -cp "$(BIN_DIR):$(JAR)" $(MAIN)

clean:
	@echo "Cleaning..."
	@rm -rf $(BIN_DIR)
