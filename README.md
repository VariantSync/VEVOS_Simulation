# VEVOS - Variant Generation

## begin todos
- Find names for
  - **ground truth extraction**: VEVOS_Extraction, VEVOSX, VEvoSX, VEVOS.extraction
  - **variant generation**: VEVOS_Generation, VEVOSG, VEvoSG, VEVOS.generation
- create a release with a jar
## end todos

VEVOS is a tool suite for the simulation of the evolution of clone-and-own projects and consists of two main components: The ground truth extraction, called VEVOS_Extraction and the variant generation called VEVOS_Generation.

This repository contains VEVOS_Generation and thus the second part of the replication package for the paper _Simulating the Evolution of Clone-and-Own Projects with VEVOS_ submitted to the International Conference on Evaluation and Assessment in Software Engineering (EASE) 2022.
VEVOS_Generation is a java library for generating variants with ground truth from an input software product line and dataset extracted with VEVOS_Extraction.

## Example Usage and Main Features

VEVOS_Generation is supposed to be used by your research prototype on clone-and-own or variability in software systems.
In the following we give a step by step example in how the library can be used to 
  - parse the ground truth dataset extracted by VEVOS_Extraction,
  - traverse the datasets' evolution history,
  - sample variants randomly, or use a predefined set of variants for generation,
  - generate variants for each step in the evolution history,
  - obtain the ground truth of generated variants.
The examples source code can also be found in [GenerationExample.java](src/main/java/vevos/examples/GenerationExample.java).
We also give a brief introduction of the key features of the library we use in the following example.

At the very begin of your program, you have to initialize the library:
```java
VEVOS.Initialize();
```
This initializes the libraries logging and binding to FeatureIDE.

We can then start by specifying the necessary paths to (1) the git repository of the input software product line, (2) the directory of the extracted ground truth dataset, (3) and a directory to which we want to generate variants. (We use case sensitive paths to also allow the generation of Linux variants under Windows).
```java
final CaseSensitivePath splRepositoryPath = CaseSensitivePath.of("path", "to", "SPL", "git", "repository");
final CaseSensitivePath groundTruthDatasetPath = CaseSensitivePath.of("path", "to", "datasets");
final CaseSensitivePath variantsGenerationDir = CaseSensitivePath.of("directory", "to", "put", "generated", "variants");
```
We can now load the extraced ground truth dataset:
```java
final VariabilityDataset dataset = Resources.Instance()
        .load(VariabilityDataset.class, groundTruthDatasetPath.path());
```
For loading data, VEVOS_Generation uses a central service for resource loading and writing called `Resources`.
`Resources` provides a unified interface for reading and writing data of any type.
Above, we use the resources to load a `VariabilityDataset` from the given path.
Internally, `Resources` stores `ResourceLoader` and `ResourceWriter` objects that perform the file system interaction.
This central interface allows users to add loaders and writers for further or custom data types as well as to replace existing loaders.
Currently, `Resources` support IO of CSV files, feature models (KernelHaven `json`, and FeatureIDE `dimacs`, `xml`), variant configurations (FeatureIDE `xml`), and presence conditions of product lines and variants.

From the loaded `dataset`, we can obtain the available evolution step.
An evolution step describes a commit-sized change to the input software product line, and is defined by the (child) commit performing a change to a previous (parent) commit.
Note that the evolution steps are not ordered because commits in the input product-line repository might not have been ordered as the commits might have been extracted from different branches.
Alternatively, we can also request a continuous history of evolution steps instead of an unordered set.
Therefore, a `SequenceExtractor` is used to determine how the successfully extracted commits should ordered.
In this example, we use the `LongestNonOverlappingSequences` extractor to sort the commits into one single continuous history.
Nevertheless, merge commits and error commits (where VEVOS_Extraction failed) are excluded from the history and thus, the returned list of commits has gaps.
Because of these gaps, we obtain a list of sub-histories, where each sub-history is continuous but sub-histories are divided by merge and error commits.
```java
final Set<EvolutionStep<SPLCommit>> evolutionSteps = dataset.getEvolutionSteps();

/// Organize all evolution steps into a history for the clone-and-own project.
final VariabilityHistory history = dataset.getVariabilityHistory(new LongestNonOverlappingSequences());
/// This yields a list of continuous sub-histories.
/// The history is divided into sub-histories because for some commits in the SPL, the commit extraction might have failed.
/// If the extraction fails for a commit c, then we have to exclude c from the variant generation.
/// This cuts the evolution history into two pieces.
/// Thus, we divide the history into sub-histories at each failed commit.
final NonEmptyList<NonEmptyList<SPLCommit>> sequencesInHistory = history.commitSequences();
```
To generate variants, we have to specify which variants should be generated.
Therefore, a `Sampler` is used that returns the set of variants to use for a certain feature model.
Apart from the possibility of introducing custom samplers, VEVOS_Generation comes with two built-in ways for sampling:
Random configuration sampling using the FeatureIDE library, and constant sampling.
Random sampling returns a random set of valid configuration from a given feature model.
Constant sampling uses a pre-defined set of variants to generate ignoring the feature model.
The set of desired variants is encapsulated in samplers because the set of valid variants of the input product line may change when the feature model changes.
Thus, the sampler can be invoked during each step of the variant generation.
```java
/// Either use random sampling, ...
final int numberOfVariantsToGenerate = 42;
Sampler variantsSampler = FeatureIDESampler.CreateRandomSampler(numberOfVariantsToGenerate);
```
```java
/// ... or use a predefined set of variants.
final Sample variantsToGenerate = new Sample(List.of(
        new Variant("Bernard", new SimpleConfiguration(List.of(
                /// Features selected in variant Bernhard.
                "A", "B", "D", "E", "N", "R"
        ))),
        new Variant("Bianca", new SimpleConfiguration(List.of(
                /// Features selected in variant Bianca.
                "A", "B", "C", "I", "N"
        )))
));

Sampler variantsSampler = new ConstSampler(variantsToGenerate);
```
We are now ready to traverse the evolution history to generate variants:
```java
for (final NonEmptyList<SPLCommit> subhistory : history.commitSequences()) {
    for (final SPLCommit splCommit : subhistory) {
        final Lazy<Optional<IFeatureModel>> loadFeatureModel = splCommit.featureModel();
        final Lazy<Optional<Artefact>> loadPresenceConditions = splCommit.presenceConditions();
```
The history we retrieved earlier is structured into sub-histories. For each sub-history we can get the commits (as objects of type `SPLCommit`) from the input software product line that was analysed by VEVOS_Extraction.
Through an `SPLCommit`, we can access the feature model and the presence condition of the software product line at the respective commit.
However, both types of data are not directly accessible but have to be loaded first.
This is what the `Lazy` type is used for: It defers the loading of data until it is actually required.
This makes accessing the possibly huge (93GB for 13k commits of Linux, yikes!) ground truth dataset faster and memory-friendly as only required data is loaded into memory.
We can start the loading process by invoking `Lazy::run` that returns a value of the loaded type (i.e., `Optional<IFeatureModel>` or `Optional<Artefact>`).
A `Lazy` caches its loaded value so loading is only performed once.
(Loaded data that is not required anymore can and should be freed by invoking `Lazy::forget`.)
As the extraction of feature model or presence condition might have failed, both types are again wrapped in an `Optional` that contains a value if extraction was successful.
Let's assume the extraction succeeded by just invoking `orElseThrow` here.
```java
        final Artefact pcs = loadPresenceConditions.run().orElseThrow();
        final IFeatureModel featureModel = loadFeatureModel.run().orElseThrow();
```
Having the feature model at hand, we can now sample the variants we want to generate for the current `splCommit`.
In case the `variantsSampler` is actually a `ConstSampler` (see above), it will ignore the feature model and will just always return the same set of variants you specified earlier in the `ConstSampler`.
```java
        final Sample variants = variantsSampler.sample(featureModel);
```
Optionally, we might want to filter which files of a variant to generate.
For example, a study on evolution of code in variable software systems could be interested only in generating the changed files of a commit.
In our case, let's just generate all variants.
Moreover, `VariantGenerationOptions` allow to configure some parameters for the variant generation.
Here, we just instruct the generation to exit in case an error happens but we could for example also instruct it to ignore errors and proceed.
```java
        final ArtefactFilter<SourceCodeFile> artefactFilter = ArtefactFilter.KeepAll();
        final VariantGenerationOptions generationOptions = VariantGenerationOptions.ExitOnError(artefactFilter);
```
Finally, we may indeed generate our variants:
```java
        for (final Variant variant : variants) {
            /// Let's put the variant into our target directory but indexed by commit hash and its name.
            final CaseSensitivePath variantDir = variantsGenerationDir.resolve(splCommit.id(), variant.getName());
            final Result<GroundTruth, Exception> result =
                pcs.generateVariant(variant, splRepositoryPath, variantDir, generationOptions);
```
The generation returns a `Result` that either represents the ground truth for the generated variant, or contains an exception if something went wrong.
In case the generation was successful, we can inspect the `groundTruth` of the variant.
The `groundTruth` consists of
- the presence conditions and feature mappings of the variant (which are different from the software product lines presence conditions, for example because line numbers shifted),
- and a block matching that for each source code file (key of the map) tells us which blocks of source code in the variant steam from which blocks of source code in the software product line.
We may also export ground truth data to disk for later usage.

(Here it is important to export the ground truth as `.variant.csv` as this suffix is used by our `Resources` to correctly load the ground truth.
In contrast, the suffix is `.spl.csv` for ground truth presence conditions of the input software product line. The major difference here is that some line numbers have to be interpreted differently upon read and write because variants are stripped off their annotations while product lines still have them.)
```java
            if (result.isSuccess()) {
                final GroundTruth groundTruth = result.getSuccess();/// 1. the presence conditions.
                final Artefact presenceConditionsOfVariant = groundTruth.variant();
                /// 2. a map that stores matchings of blocks for each source code file
                final Map<CaseSensitivePath, AnnotationGroundTruth> fileMatches = groundTruth.fileMatches();

                /// We can also export the ground truth PCs of the variant.
                Resources.Instance().write(Artefact.class, presenceConditionsOfVariant, variantDir.resolve("pcs.variant.csv").path());
            } 
```
This was round-trip about the major features of VEVOS_Generation. Further features and convencience methods can be found in our documentation.

## Project Structure

The project is structured into the following packages:
- [`vevos.examples`](src/main/java/vevos/examples) contains the code of our example described above
- [`vevos.feature`](src/main/java/vevos/feature) contains our representation for `Variant`s and their `Configuration`s as well as sampling of configurations and variants
- [`vevos.io`](src/main/java/vevos/io) contains our `Resources` service and default implementations for loading `CSV` files, ground truth, feature models, and configurations
- [`vevos.repository`](src/main/java/vevos/repository) contains classes for representing git repositories and commits
- [`vevos.sat`](src/main/java/vevos/sat) contains an interface for SAT solving (currently only used for annotation simplification on demand)
- [`vevos.util`](src/main/java/vevos/util) is the conventional utils package with helper methods for interfacing with FeatureIDE, name generation, logging, and others.
- [`vevos.variability`](src/main/java/vevos/variability) contains the classes for representing evolution histories and the ground truth dataset.
  The package is divided into:
    - [`vevos.variability.pc`](src/main/java/vevos/variability/pc) contains classes for representing , and annotations (i.e., presence conditions and feature mappings). We store annotations in `Artefact`s that follow a tree structure similar to the annotations in preprocessor based software product lines.
    - [`vevos.variability.pc.groundtruth`](src/main/java/vevos/variability/pc/groundtruth) contains datatypes for the ground truth of generated variants
    - [`vevos.variability.pc.options`](src/main/java/vevos/variability/pc/options) contains the options for the variant generation process
    - [`vevos.variability.pc.visitor`](src/main/java/vevos/variability/pc/visitor) contains an implementation of the visitor pattern for traversing and inspecting `ArtefactTree`s. Some visitors for querying a files or a line's presence condition, as well as a pretty printer can be found in `vevos.variability.pc.visitor.common`.
    - [`vevos.variability.sequenceextraction`](src/main/java/vevos/variability/pc/sequenceextraction) contains default implementation for `SequenceExtractor`. These are algorithms for sorting pairs of commits into continuous histories (see example above).

## Setup

VEVOS_Generation is a Java 16 library and Maven project.
You may include VEVOS_Generation as a pre-build `jar` file or build it on your own.
The `jar` file can be found in the releases of this repository.

### Build

VEVOS_Generation comes with three dependencies that are not available in the maven repositories and thus are included as pre-built jar files in `src/main/resources/lib`.
To initialize the local maven repository for the libraries we use, run the following as maven targets:

- FeatureIDE: `deploy:deploy-file -DgroupId=de.ovgu -DartifactId=featureide.lib.fm -Dversion=3.7.2 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=src/main/resources/lib/de.ovgu.featureide.lib.fm-v3.7.2.jar`
- Sat4j: `deploy:deploy-file -DgroupId=org.sat4j -DartifactId=core -Dversion=2.3.5 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=src/main/resources/lib/org.sat4j.core.jar`
