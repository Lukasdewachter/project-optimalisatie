## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).

# TODO
Simulated annealing

1. start solution s0 en start temperature t0
2. t / (1+bt) afkoeling
3. Itereren door solutions totdat eindtemp bereikt wordt
4. Nieuwe oplossing genereren door bv swappen van 2 jobs
4.1 Als het beter is -> accepteer, als slechter -> genereer random tussen 0 en 1 en accepteer als onder t