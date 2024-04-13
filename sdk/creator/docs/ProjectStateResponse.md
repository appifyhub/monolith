# ProjectStateResponse

A representation of the project's current state

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**status** | **str** | The status of the project | 
**usable_features** | [**List[ProjectFeatureResponse]**](ProjectFeatureResponse.md) | The usable features of the project | 
**unusable_features** | [**List[ProjectFeatureResponse]**](ProjectFeatureResponse.md) | The unusable features of the project | 

## Example

```python
from appifyhub.models.project_state_response import ProjectStateResponse

# TODO update the JSON string below
json = "{}"
# create an instance of ProjectStateResponse from a JSON string
project_state_response_instance = ProjectStateResponse.from_json(json)
# print the JSON string representation of the object
print(ProjectStateResponse.to_json())

# convert the object into a dict
project_state_response_dict = project_state_response_instance.to_dict()
# create an instance of ProjectStateResponse from a dict
project_state_response_from_dict = ProjectStateResponse.from_dict(project_state_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


