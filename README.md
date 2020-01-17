Experimental Scala DSL for writing Terraform configurations

## Project status

It is in initial development phase.

It should be considered as an experiment or proof of concept rather than production-ready project.

## Motivation

I really like the idea of Infrastructure as Code implemented in Terraform. However, writing Terraform configurations in
 HCL feels clunky for Scala developer. Lack of types, no compilation phase to catch apparent mistakes, weird treatment 
 of booleans and a number of other oddities do not make that experience optimal.

The goal of this project is to provide typesafe DSL for writing Teraform configuration in Scala. It is implemented in 
`templating` module.

Having DSL is not enough though to be able to be productive in writing configurations. You need all your providers to be
 expressed as Scala code too. This process can be automated to some extend. Generating Scala code out of Provider's 
 Schema is the objective of `parse` and `codegen` submodules.

## How is Terraform configuration generated out of Scala code

The core idea is as follows:

You write Scala code to define your configuration. That code is compiled which by itself can ensure some invariants. 
Then by running the result of compilation you obtain your configuration in Terraform 
[JSON configuration syntax](https://www.terraform.io/docs/configuration/syntax-json.html).

## Terraform4s providers

The DSL itself describes only what are the basic concepts of Terraform, e.g. `Resource`. The DSL does not contain any 
information about concrete providers at all. They are supposed to be provided by community as Scala libraries 
implementing certain structures defined in `terraform4s`. We'll refer to them as "terraform4s providers" in the rest of
the document.

### Code generation of terraform4s providers

If the provider you need is not available then you need to write it by yourself. Fortunately this process can be 
automated to some extend by generating basic Scala types out of provider schema. It's the goal of `codegen` module.

`codegen` takes the output of the command `terraform providers schema -json` and generates Scala ADT out of it. 
`codegen` can be used both programatically as a Scala library or as a CLI. The types in generated ADT are not very 
precise but that is caused by types being imprecise on schema level. Thus, it is recommended for ease of use to write 
additional layer on top of generated facades.
