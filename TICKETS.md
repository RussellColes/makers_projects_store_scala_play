# Tickets

Start by completing key tickets before moving on to the stretch ones.

Please remember that your focus is to work on the backend. If you feel the views
are holding you back, we'd recommend that you use Postman as your client to
interact with your API.

## Key Requirements

### Authentication

1. Finish implementing Login Functionality (Severus has been drawn to another project!)
- [ ] Implement the `logIn` action in the `UserController` to authenticate users
  using their username and password.

2. Add Session (Cookie-Based) authentication
- [ ] Implement session management to keep users logged in after they
  successfully authenticate.

<!-- OMITTED -->

### Items

This is an e-commerce website, but we have no products yet! We need some sort
Item entity introduced in our application. It should have the following
properties: `name`, `price`, `description`.

We need to fully build our app so that our users can:
- [ ] Create, update, delete and fetch items.
- [ ] Fetch a particular item by it's ID.


### Cart

We would like our users to be able to add, remove and view items to their cart. 

- [ ] Implement a Cart feature that covers the above functionalities.


## Stretch

**Location**

The marketplace is so far global we can assume. However, we'd like to start
targeting specific regions and advertising our products in these regions only.

For this, we'd need to introduce the concept of `Location` in our application.
The idea is that we are able to:

- [ ] Fetch all Items available in a particular location (either by name or id).
- [ ] Fetch all locations in a given country (e.g. "ES", "UK", "US", "FR").

**Payments (only backend)**

Implementing a fictional payment system will involve creating a basic checkout
process and simulating payment processing. Here are more detailed tasks:

1. Payment Models:

- [ ] Create a Payment model with properties such as `amount`, `currency`, `status`,
  `user_id`, and `order_id`.
- [ ] Create an Order model with properties such as `user_id`, `items` (list of item
  IDs), `total_amount`, and `status`.

2. Payment Processing:

- [ ] Implement a service to handle payment processing logic. This service
  should include methods to at least create a payment.

3. Endpoints:

- [ ] Create an endpoint to initiate a payment. This endpoint should receive
  order details and return a confirmation.

**Extension research**

:bulb: How can you ensure payment info is as secure as possible? Should you use
a third party?
