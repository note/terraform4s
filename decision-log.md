## Codegen

1. The output structure (e.g. one resource case class per file) is optimized for ease of implementation. If it's
proven to be useful it should be optimized for other concerns. Generating `Anonym_X` case classes for each 
HCL `object` is possibly the worst model as its naming conveys no meaning and it does not support code reusage
in case if given structure repeats across many terraform resources. Also, structural types might be considered
as a better choice.

2. Nested objects are not su~~~~pported yet.