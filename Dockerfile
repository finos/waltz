# Pull base image.
FROM openjdk:8


RUN mkdir /waltz
WORKDIR /waltz
ADD . /waltz

# Define default command.
CMD ["bash"]