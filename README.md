# Ver-ID-Serialization-Android

Serialize common Ver-ID types using protocol buffers

## Installation

1. Add the following repository to your project's repositories:
  
  ```groovy
  maven { url 'https://jitpack.io' }
  ```
2. Add the following dependency:
  
  ```groovy
  implementation 'com.appliedrec.verid:serialization:1.0.0'
  ```
  
## Usage

### Serializing Ver-ID types

In this example we take a Ver-ID face and serialize it into a byte array.

```java
Face verIDFace; // Face detected by Ver-ID
// Create an instance of ProtobufTypeConverter
ProtobufTypeConverter converter = new ProtobufTypeConverter();
// Convert the Ver-ID face to protobuf face
com.appliedrec.verid.proto.Face protobufFace = converter.convertFace(verIDFace);
// Serialize the protobuf face
byte[] serializedFace = protobufFace.toByteArray();
```

### Deserializing Ver-ID types

This is the previous example in reverse.

```java
byte[] serializedFace; // Serialized face
// Deserialize the byte array to protobuf face
com.appliedrec.verid.proto.Face protobufFace = com.appliedrec.verid.proto.Face.parseFrom(serializedFace);
// Create an instance of ProtobufTypeConverter
ProtobufTypeConverter converter = new ProtobufTypeConverter();
// Convert the protobuf face to Ver-ID face
Face veridFace = converter.convertFace(protobufFace);
```

Please refer to the [documentation](https://appliedrecognition.github.io/Ver-ID-Serialization-Android/) for additional types.
