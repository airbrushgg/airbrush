# Bamboo
Bamboo is a fast and lightweight schematic library for Minestom.

## Install
Bamboo is available on [Maven Central](https://central.sonatype.com/artifact/dev.flavored/bamboo)

```kts
dependencies {
    implementation("dev.flavored:bamboo:0.1.0")
}
```

## Example
The following example loads a schematic from a file, then pastes it into the `instance` at a given position.
```java
Schematic schematic = Bamboo.fromFile("example.schematic", new SchematicOptions());
schematic.paste(instance, new Pos(0.0, 0.0, 0.0));
```

There are two other ways to load schematics: from a stream and from a `java.nio.file.Path`.
```java
Bamboo.fromStream(inputStream, new SchematicOptions());
Bamboo.fromPath(path, new SchematicOptions());
```

## License
Bamboo is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.