# Java_Enterprise_Project_1
First Project for my class on java enterprise

Purpose is a simple program that allows for users to create accounts and request reimbursment for work related expenses from their employers

Features:

Create a user : requests username and password and stores them, allowing later login

STRETCH GOAL : Hash passwords to maintain security

Permission levels:

Employee (Default): Can submit requests and view previous requests made by same user

Manager : Can view all requests, and approve or deny pending requests, can also change permission level of other users

(STRETCH GOAL) Administrator : Has all permissions of a manager and can change permission level of users, in this case managers CANNOT change permissions

Requests: 
Employees can submit requests for reimbursment

Includes : Description of cost and value
STRETCH : Can specify type
Immediately set to "pending" upon submission

Approval / Denial:
Managers can either approve or deny requests that are currently marked as pending
