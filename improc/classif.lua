
-- нужно запустить с именем картинки из папки test_path

require 'torch'
require 'optim'
require 'xlua'
require 'image'
require 'nn'
require 'nngraph'

local config = require 'config'

local model_file = config.modelPath .. 'model'
local test_path = config.pathToTestImages

torch.setdefaulttensortype('torch.DoubleTensor')
local channels = config.channels
local size = config.size
local categories = config.categories
local name_img = arg[1]

local m = torch.load(model_file)
local input = image.load(test_path .. name_img)
local inp = torch.Tensor(input)
local predicted = m:forward(inp)

--image.display(input)
print("predicted: ")
print(predicted)
local p = torch.exp(predicted)
local mx, max_i = torch.max(p, 1)

for i = 1, predicted:size(1) do
  if (max_i[1] == i) then
    print(sys.COLORS.green .. name_img, categories[i], torch.exp(predicted[i]))
  else
    --print(sys.COLORS.white .. categories[i], torch.exp(predicted[i]))
  end
  p = p + torch.exp(predicted[i])
end
