# UserSignupRequest

A request to sign up a new user. Depending on the project configuration, user ID can be either auto-generated or required. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**user_id** | **str** | A unique user identifier within the project. Depending on the project configuration, it can be differently formatted or even auto-generated.  | [optional] 
**raw_signature** | **str** | The secret signature of the user, usually a plain-text password | 
**name** | **str** | The name of the user | [optional] 
**type** | [**UserType**](UserType.md) |  | [optional] 
**allows_spam** | **bool** | Whether the user allows spam or not | [optional] 
**contact** | **str** | The contact channel for the user (depends on the contact type) | [optional] 
**contact_type** | [**UserContactType**](UserContactType.md) |  | [optional] 
**birthday** | **date** | The birthday of the user | [optional] 
**company** | [**OrganizationDto**](OrganizationDto.md) |  | [optional] 
**language_tag** | **str** | The default language of the user (locale represented as in IETF BCP 47) | [optional] 
**signup_code** | **str** | The signup code to use for sign up this creator (usually required by default) | [optional] 

## Example

```python
from appifyhub.models.user_signup_request import UserSignupRequest

# TODO update the JSON string below
json = "{}"
# create an instance of UserSignupRequest from a JSON string
user_signup_request_instance = UserSignupRequest.from_json(json)
# print the JSON string representation of the object
print(UserSignupRequest.to_json())

# convert the object into a dict
user_signup_request_dict = user_signup_request_instance.to_dict()
# create an instance of UserSignupRequest from a dict
user_signup_request_from_dict = UserSignupRequest.from_dict(user_signup_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


