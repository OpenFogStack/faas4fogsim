# FaaS4FogSim

This repository contains the source code for the simulation software used within the paper **Towards Auction-Based Function Placement in Serverless Fog Platforms**:

The Function-as-a-Service (FaaS) paradigm has a lot of potential as a computing model for fog environments comprising both cloud and edge nodes.
When the request rate exceeds capacity limits at the edge, some functions need to be offloaded from the edge towards the cloud.
In this position paper, we propose an auction-based approach in which application developers bid on resources.
This allows fog nodes to make a local decision about which functions to offload while maximizing revenue.
For a first evaluation of our approach, we use simulation.

If you use this software in a publication, please cite it as:

### Text

David Bermbach, Setareh Maghsudi, Jonathan Hasenburg, Tobias Pfandzelter. **Towards Auction-Based Function Placement in Serverless Fog Platforms.** In: Proceedings of the Second IEEE International Conference on Fog Computing 2020 (ICFC 2020). IEEE 2020.

### BibTeX
```
@inproceedings{paper_bermbach_auctions4function_placement,
	title = {Towards Auction-Based Function Placement in Serverless Fog Platforms},
	booktitle = {Proceedings of the Second {IEEE} {International} {Conference} on {Fog} {Computing} (ICFC 2020)},
	author = {Bermbach, David and Maghsudi, Setareh and Hasenburg, Jonathan and Pfandzelter, Tobias},
	year = {2020},
	publisher = {IEEE}
}
```

## Using the simulation

- Import the project in IntelliJ as Gradle Project
- Run MainKt and provide as argument:
    - `sim1`: study the effect of an increasing request load on the processing prices
    - `sim2`: study the effect of an increasing number of executables on storage prices
    - `sim3`: study the effect of node disloyalty (in the paper stickiness, 100% disloyal = 0% sticky)
- Parameter choices for sim1 and sim2 are detailed in `simResults/parameter_choices_paper_eval.txt`
- The simulation is deterministic, the results and some analysis results for sim1 and sim2 are also available in `simResults`
- You can customize simulation parameters in the CONFIG class
