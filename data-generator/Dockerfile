FROM python:3.10-slim
WORKDIR /app

# Copy all files from build context to WORKDIR
COPY . .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Entrypoint configuration
ENTRYPOINT ["python", "data_generator.py"]
CMD []