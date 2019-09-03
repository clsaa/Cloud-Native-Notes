#!groovy
pipeline{
  agent any
  environment{
    REPOSITORY="ssh://git"
  }
}