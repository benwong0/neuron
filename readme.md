# What is it?
Neuron is an abstract layer between the DJI drone SDK and your application. It follows an easy to understand observer pattern to retrieve information from the drone SDK. It also allows listening for a combination of drone status as well as minimizing duplicated callbacks.
# Why do you need it?
Currently, DJI drone SDK provides many callbacks for developers to hook into in order to receive information. In a large application, you often need to listen for the same information in different parts of your app. Managing all these listeners will get confusingly complicated very quickly as you require more information from the DJI SDK.
# Goals
The goal of Neuron is to help simplify drone development. Hopefully it will allow you to build applications using the DJI SDK much faster while maintaining a clean and easy to understanding code base.