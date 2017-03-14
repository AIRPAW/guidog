
-- в качетсве первого аргумента задается путь к директории с тестовыми картинками
-- второй аргумент - тип требуемого элемента

require 'torch'
require 'optim'
require 'xlua'
require 'image'
require 'nn'
require 'nngraph'
require 'paths'

local config = require 'config'
local threads = require 'threads'
local nthreads = config.nthreads
file = io.open('classifLog.txt','w')
-- таблица для хранения батча элементов для распознавания
-- при инициализации содержит таблицу с картинкой, предсказанной вероятностью,
-- равной 0, предсказанный класс, равный пустой строке,
local predictedElements = {}
function newElement(t, img)
    local element = {image = img, p = 0, label = '', accordindex = (#predictedElements + 1)}
    element.getMeta = function(t)
      print(t.p, t.label, t.accordindex)
    end
    table.insert(t, element)
end

function filterByType(t, type)
  local typifidElements = {}
  for key, el in pairs(t) do
    if el.label == type then table.insert(typifidElements, el) end
  end
  return typifidElements
end

function filterByMaxP(t, type)
  local filtred = filterByType(t, type)
  local max = 0
  local index = 0
  for key, el in pairs(filtred) do
      if max < el.p then
        max = el.p
        index = key
      end
  end
  return filtred[index], index
end

local model_file = config.modelPath .. 'model'
torch.setdefaulttensortype('torch.DoubleTensor')
local channels = config.channels
local size = config.size
local categories = config.categories
local imgPath = arg[1]
local m = torch.load(model_file)
local elementType = arg[2]

for file in paths.iterfiles(imgPath) do
  newElement(predictedElements, image.load(imgPath .. file))
end

local nprocess = 10
local resultTable = {}
local pool = threads.Threads(
   nthreads,
   function()
     require "nn"
   end,
   function()
      l_model = m:clone()
   end
 )

local time = sys.clock()
for key, el in pairs(predictedElements) do
  pool:addjob(
  function()
    local inp = torch.Tensor(el.image)
    local p = torch.exp(l_model:forward(inp))
    local mx, max_i = torch.max(p, 1)
    el.p = mx[1]
    el.label = categories[max_i[1]]
    return el
  end,
  function(element)
    table.insert(resultTable,element)
  end
)
end
pool:terminate()
time = sys.clock() - time
file:write('time to classify ' .. #predictedElements .. ' elements ' .. (time*1000) .. 'ms \n')
file:close()
-- for key, el in pairs(filterByType(predictedElements, elementType)) do
--   el:getMeta()
-- end
print(filterByMaxP(resultTable, elementType):getMeta())
